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
package org.geoserver.gwc.blobstore.latest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geoserver.gwc.blobstore.readonlyfile.ReadOnlyFileBlobStore;
import org.geowebcache.config.ConfigurationException;
import org.geowebcache.io.ByteArrayResource;
import org.geowebcache.io.FileResource;
import org.geowebcache.io.Resource;
import org.geowebcache.storage.DefaultStorageFinder;
import org.geowebcache.storage.StorageException;
import org.geowebcache.storage.TileObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A file blob store that assumes all tiles exists for where there is data coverage.
 * A bank tile will be returned if no tile is found in the underlying storage.
 * Tiles willnot be created.
 * All delete methods are no longer supported to protect the underlying tiles from deletion.
 * @author mike.dolding
 */
public class LatestFileBlobStore extends ReadOnlyFileBlobStore  {

    private static Log log =
            LogFactory.getLog(LatestFileBlobStore.class);

    private String match;

    private String replace;

    private String[] replacements;

    public LatestFileBlobStore(DefaultStorageFinder defStoreFinder)
            throws StorageException, ConfigurationException {
        super(defStoreFinder);
    }


    public LatestFileBlobStore(String rootPath) throws StorageException {
        super(rootPath);
    }

    /**
     * Set the blob property of a TileObject.
     * Return the tile from the underlying store.
     * If the the tile does not exist it will a blank tile.
     * @param stObj the tile to load. Its setBlob() method will be called.
     * @return true will always return a tile
     */
    public boolean get (TileObject stObj) throws StorageException {
        List<File> files = getMatchingFiles(stObj);
        Resource resource = null;
        if (files.isEmpty()) {
            resource =  getBlankTile();;
        } else if (files.size() == 1) {
            resource =  new FileResource(files.get(0));
        } else {
            resource = mergeTiles(files);
        }
        stObj.setBlob(resource);
        stObj.setCreated(resource.getLastModified());
        stObj.setBlobSize((int) resource.getSize());
        return true;
    }

    private Resource mergeTiles(List<File> tiles) throws StorageException {

        try {
            BufferedImage merged = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = merged.createGraphics();
            ImageIO.setUseCache(false);
            for (File tile : tiles) {
                BufferedImage image = ImageIO.read(tile);
                g.setComposite(AlphaComposite.SrcOver);
                g.drawImage(image, 0, 0, null);
            }
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(merged, "PNG", out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Unable to merge tiles: "
                    + tiles.stream()
                    .map( f -> f.getAbsolutePath())
                    .collect(Collectors.joining(",")), e);
            throw new StorageException("Unable to merge tiles", e);
        }

    }

    List<File> getMatchingFiles(TileObject stObj) throws StorageException {
        List<File> files = new ArrayList<>();
        File template = getFileHandleTile(stObj);
        for (String replacement : replacements) {
            File file = mockTile(new File(template.getAbsolutePath().replaceAll(match, replacement)));
            if(file.exists()) {
                files.add(file);
            }
        }
        return files;
    }

    public String getMatch () {
        return match;
    }

    public void setMatch (String match){
        this.match = match;
    }

    public String getReplace () {
        return replace;
    }

    public void setReplace (String replace){
        this.replace = replace.replaceAll(" ", "");
        this.replacements = replace.split(",");
    }

}