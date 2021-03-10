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
 * @author Arne Kepp / The Open Planning Project 2009
 */
/**
 * MCD
 * Copy of usual FileBlobStore but will return .png OR png8 cached files.
 *
 */
package org.geoserver.gwc.blobstore.pngagnostic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.config.ConfigurationException;
import org.geowebcache.filter.parameters.ParametersUtils;
import org.geowebcache.io.FileResource;
import org.geowebcache.io.Resource;
import org.geowebcache.mime.MimeException;
import org.geowebcache.mime.MimeType;
import org.geowebcache.storage.DefaultStorageFinder;
import org.geowebcache.storage.StorageException;
import org.geowebcache.storage.StorageObject.Status;
import org.geowebcache.storage.TileObject;
import org.geowebcache.storage.blobstore.file.FileBlobStore;
import org.geowebcache.storage.blobstore.file.FilePathGenerator;

import java.io.File;
import java.util.Objects;

import static org.geowebcache.storage.blobstore.file.FilePathUtils.filteredLayerName;

/** See BlobStore interface description for details */
public class PngAgnosticFileBlobStore extends FileBlobStore {

    private static Log log =
            LogFactory.getLog(PngAgnosticFileBlobStore.class);

    private final String path;

    private FilePathGenerator pathGenerator;

    public PngAgnosticFileBlobStore(DefaultStorageFinder defStoreFinder)
            throws StorageException, ConfigurationException {
        this(defStoreFinder.getDefaultPath());
    }

    public PngAgnosticFileBlobStore(String rootPath) throws StorageException {
        super(rootPath);
        this.path = rootPath;
        pathGenerator = new FilePathGenerator(this.path);

    }

    /**
     * Set the blob property of a TileObject.
     *
     * @param stObj the tile to load. Its setBlob() method will be called.
     * @return true if successful, false otherwise
     */
    public boolean get(TileObject stObj) throws StorageException {
        File fh = PngAgnosticFile.getPngOrPng8File(getFileHandleTile(stObj, false));
        if (!fh.exists()) {
            stObj.setStatus(Status.MISS);
            return false;
        } else {
            Resource resource = readFile(fh);
            stObj.setBlob(resource);
            stObj.setCreated(resource.getLastModified());
            stObj.setBlobSize((int) resource.getSize());
            return true;
        }
    }

    private File getFileHandleTile(TileObject stObj, boolean create) throws StorageException {
        final MimeType mimeType;
        try {
            mimeType = MimeType.createFromFormat(stObj.getBlobFormat());
        } catch (MimeException me) {
            log.error(me.getMessage());
            throw new RuntimeException(me);
        }

        final File tilePath = pathGenerator.tilePath(stObj, mimeType);

        if (create) {
            File parent = tilePath.getParentFile();
            mkdirs(parent, stObj);
        }


        return tilePath;
    }

    private Resource readFile(File fh) throws StorageException {
        if (!fh.exists()) {
            return null;
        }
        return new FileResource(fh);
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

}
