/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * <p>Copyright 2019
 */
package org.geoserver.gwc.blobstore.readonlyfile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheDispatcher;
import org.geowebcache.config.ConfigurationException;
import org.geowebcache.filter.parameters.ParametersUtils;
import org.geowebcache.io.ByteArrayResource;
import org.geowebcache.io.FileResource;
import org.geowebcache.io.Resource;
import org.geowebcache.mime.MimeException;
import org.geowebcache.mime.MimeType;
import org.geowebcache.storage.*;
import org.geowebcache.storage.blobstore.file.FilePathGenerator;
import org.geowebcache.util.FileUtils;
import org.geowebcache.util.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.geowebcache.storage.blobstore.file.FilePathUtils.filteredLayerName;

/**
 * A file blob store that assumes all tiles exists for where there is data coverage.
 * A bank tile will be returned if no tile is found in the underlying storage.
 * Tiles willnot be created.
 * All delete methods are no longer supported to protect the underlying tiles from deletion.
 * @author mike.dolding
 */
public class ReadOnlyFileBlobStore  implements BlobStore {

    private static Log log =
            LogFactory.getLog(ReadOnlyFileBlobStore.class);

    protected final String path;

    protected final BlobStoreListenerList listeners = new BlobStoreListenerList();

    protected FilePathGenerator pathGenerator;

    private static Resource blankTile = null;

    public ReadOnlyFileBlobStore(DefaultStorageFinder defStoreFinder)
            throws StorageException, ConfigurationException {
        this(defStoreFinder.getDefaultPath());
    }

    public ReadOnlyFileBlobStore(String rootPath) throws StorageException {
        this.path = rootPath;
        pathGenerator = new FilePathGenerator(this.path);

        // prepare the root
        File fh = new File(path);
        fh.mkdirs();
        if (!fh.exists() || !fh.isDirectory()) {
            throw new StorageException(path + " is not a directory.");
        }
        final boolean exists = new File(fh, "metadata.properties").exists();
        boolean empty = true;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(fh.toPath())) {
            for (@SuppressWarnings("unused") Path p : ds) {
                empty = false;
                break;
            }
        } catch (StorageException e) {
            throw e;
        } catch (IOException e) {
            throw new StorageException("Error while checking that " + rootPath + " is empty", e);
        }

        CompositeBlobStore.checkSuitability(rootPath, exists, empty);

        getBlankTile();
        
    }

    /** Destroy method for Spring */
    public void destroy() {
    }

    @Override
    public boolean delete(String layerName) throws StorageException {
        log.info(String.format("Ignoring attempt to delete readonly file blob for layer %s", layerName));
        return true;
    }

    @Override
    public boolean deleteByGridsetId(String layerName, String gridSetId) throws StorageException {
        throw new UnsupportedOperationException("Store is readonly");
    }

    /**
     * Renames the layer directory for layer {@code oldLayerName} to {@code newLayerName}
     *
     * @return true if the directory for the layer was renamed, or the original directory didn't
     *     exist in first place. {@code false} if the original directory exists but can't be renamed
     *     to the target directory
     * @throws StorageException if the target directory already exists
     * @see BlobStore#rename
     */
    public boolean rename(final String oldLayerName, final String newLayerName)
            throws StorageException {
        final File oldLayerPath = getLayerPath(oldLayerName);
        final File newLayerPath = getLayerPath(newLayerName);

        if (newLayerPath.exists()) {
            throw new StorageException(
                    "Can't rename layer directory "
                            + oldLayerPath
                            + " to "
                            + newLayerPath
                            + ". Target directory already exists");
        }
        if (!oldLayerPath.exists()) {
            this.listeners.sendLayerRenamed(oldLayerName, newLayerName);
            return true;
        }
        if (!oldLayerPath.canWrite()) {
            log.info(oldLayerPath + " is not writable");
            return false;
        }
        boolean renamed = FileUtils.renameFile(oldLayerPath, newLayerPath);
        if (renamed) {
            this.listeners.sendLayerRenamed(oldLayerName, newLayerName);
        } else {
            throw new StorageException(
                    "Couldn't rename layer directory " + oldLayerPath + " to " + newLayerPath);
        }
        return renamed;
    }

    private File getLayerPath(String layerName) {
        String prefix = path + File.separator + filteredLayerName(layerName);

        File layerPath = new File(prefix);
        return layerPath;
    }

    @Override
    public boolean delete(TileObject obj) throws StorageException {
        throw new UnsupportedOperationException("Store is readonly");
    }

    @Override
    public boolean delete(TileRange obj) throws StorageException {
        throw new UnsupportedOperationException("Store is readonly");
    }

    /**
     * Set the blob property of a TileObject.
     * Return the tile from the underlying store.
     * If the the tile does not exist it will a blank tile.
     * @param stObj the tile to load. Its setBlob() method will be called.
     * @return true will always return a tile
     */
    public boolean get(TileObject stObj) throws StorageException {
        File fh = getFileHandleTile(stObj, false);
        Resource resource = fh.exists() ? new FileResource(fh) : getBlankTile();
        stObj.setBlob(resource);
        stObj.setCreated(resource.getLastModified());
        stObj.setBlobSize((int) resource.getSize());
        return true;
    }

    /**
     * Load GWC blank tile resource.
     * @return
     */
    protected static Resource getBlankTile()  {
        if (blankTile != null) {
            return blankTile;
        }
        URL url = GeoWebCacheDispatcher.class.getResource("blank.png");
        blankTile = new ByteArrayResource();
        ReadableByteChannel ch = null;
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            ch = Channels.newChannel(inputStream);
            blankTile.transferFrom(ch);
        } catch (IOException e) {
            log.error("Unable to load blank tile", e);
        } finally {
            IOUtils.closeQuietly(ch);
            IOUtils.closeQuietly(inputStream);
        }
        return blankTile;
    }

    /** Store a tile. */
    public void put(TileObject stObj) throws StorageException {
        throw new UnsupportedOperationException("Store is readonly");
    }

    protected File getFileHandleTile(TileObject stObj, boolean create) throws StorageException {
        final MimeType mimeType;
        try {
            mimeType = MimeType.createFromFormat(stObj.getBlobFormat());
        } catch (MimeException me) {
            log.error(me.getMessage());
            throw new RuntimeException(me);
        }

        File tilePath = pathGenerator.tilePath(stObj, mimeType);

        /**
         * If the client requests "png" then we will serve tiles png8 tiles instead.
         */
        if (tilePath.getAbsolutePath().endsWith("png")) {
            tilePath = new File (tilePath.getAbsolutePath() + "8");
        }

        return tilePath;
    }

    public void clear() throws StorageException {
        throw new StorageException("Not implemented yet!");
    }

    /** Add an event listener */
    public void addListener(BlobStoreListener listener) {
        listeners.addListener(listener);
    }

    /** Remove an event listener */
    public boolean removeListener(BlobStoreListener listener) {
        return listeners.removeListener(listener);
    }

    /**
     * This method will recursively create the missing directories and call the listeners
     * directoryCreated method for each created directory.
     *
     * @param path
     * @return
     */
    private boolean mkdirs(File path, TileObject stObj) {
        /* If the terminal directory already exists, answer false */
        if (path.exists()) {
            return false;
        }
        /* If the receiver can be created, answer true */
        if (path.mkdir()) {
            // listeners.sendDirectoryCreated(stObj);
            return true;
        }
        String parentDir = path.getParent();
        /* If there is no parent and we were not created, answer false */
        if (parentDir == null) {
            return false;
        }
        /* Otherwise, try to create a parent directory and then this directory */
        mkdirs(new File(parentDir), stObj);
        if (path.mkdir()) {
            // listeners.sendDirectoryCreated(stObj);
            return true;
        }
        return false;
    }

    /**
     * @see BlobStore#getLayerMetadata(String, String)
     */
    public String getLayerMetadata(final String layerName, final String key) {
        Properties metadata = getLayerMetadata(layerName);
        String value = metadata.getProperty(key);
        if (value != null) {
            value = urlDecUtf8(value);
        }
        return value;
    }

    private static String urlDecUtf8(String value) {
        try {
            value = URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    /**
     * @see BlobStore#putLayerMetadata(String, String,
     *     String)
     */
    public void putLayerMetadata(final String layerName, final String key, final String value) {
        throw new UnsupportedOperationException("Store is readonly");
    }

    private Properties getLayerMetadata(final String layerName) {
        final File metadataFile = getMetadataFile(layerName);
        Properties properties = new Properties();
        final String lockObj = metadataFile.getAbsolutePath().intern();
        synchronized (lockObj) {
            if (metadataFile.exists()) {
                FileInputStream in;
                try {
                    in = new FileInputStream(metadataFile);
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
                }
                try {
                    properties.load(in);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return properties;
    }

    private File getMetadataFile(final String layerName) {
        File layerPath = getLayerPath(layerName);
        File metadataFile = new File(layerPath, "metadata.properties");
        return metadataFile;
    }

    @Override
    public boolean layerExists(String layerName) {
        return getLayerPath(layerName).exists();
    }

    @Override
    public boolean deleteByParametersId(String layerName, String parametersId) throws StorageException {
        throw new UnsupportedOperationException("Store is readonly");
    }

    private Stream<Path> layerChildStream(
            final String layerName, DirectoryStream.Filter<Path> filter) throws IOException {
        final File layerPath = getLayerPath(layerName);
        if (!layerPath.exists()) {
            return Stream.of();
        }
        final DirectoryStream<Path> layerDirStream =
                Files.newDirectoryStream(layerPath.toPath(), filter);
        return StreamSupport.stream(layerDirStream.spliterator(), false)
                .onClose( // Delegate closing so that when the returned stream is closed, so is the
                        // underlying DirectoryStream
                        () -> {
                            try {
                                layerDirStream.close();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
    }

    @Override
    public Map<String, Optional<Map<String, String>>> getParametersMapping(String layerName) {
        Properties p = getLayerMetadata(layerName);
        return getParameterIds(layerName)
                .stream()
                .collect(
                        Collectors.toMap(
                                (id) -> id,
                                (id) -> {
                                    String kvp = p.getProperty("parameters." + id);
                                    if (Objects.isNull(kvp)) {
                                        return Optional.empty();
                                    }
                                    kvp = urlDecUtf8(kvp);
                                    return Optional.of(ParametersUtils.getMap(kvp));
                                }));
    }

    static final int paramIdLength =
            ParametersUtils.getId(Collections.singletonMap("A", "B")).length();

    @Override
    public Set<String> getParameterIds(String layerName) {
        try (Stream<Path> layerChildStream =
                     layerChildStream(layerName, (p) -> Files.isDirectory(p))) {
            return layerChildStream
                    .map(p -> p.getFileName().toString())
                    .map(s -> s.substring(s.lastIndexOf('_') + 1))
                    .filter(s -> s.length() == paramIdLength) // Zoom level should never be the same
                    // length so this should be safe
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

