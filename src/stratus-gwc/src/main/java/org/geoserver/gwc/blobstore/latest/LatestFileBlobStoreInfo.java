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

import org.geowebcache.config.BlobStoreInfo;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.locks.LockProvider;
import org.geowebcache.storage.BlobStore;
import org.geowebcache.storage.StorageException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Mirrors FileBlobStoreInfo but for readonly use with block size removed.
 * @author mike.dolding
 */
public class LatestFileBlobStoreInfo extends BlobStoreInfo  {

    private static final long serialVersionUID = 87243591561561659L;

    private String baseDirectory;

    private String match;

    private String replace;

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }

    public LatestFileBlobStoreInfo() {
        super();
    }

    public LatestFileBlobStoreInfo(String id) {
        super(id);
    }

    /**
     * Get the base directory for persisting tiles
     *
     * @return The file system path to the base directory
     */
    public String getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Set the base directory for persisting tiles
     *
     * @param baseDirectory The file system path to the base directory
     */
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public String toString() {
        return new StringBuilder("LatestFileBlobStore[id:")
                .append(getName())
                .append(", enabled:")
                .append(isEnabled())
                .append(", baseDirectory:")
                .append(baseDirectory)
                .append(", match:")
                .append(match)
                .append(", replace:")
                .append(replace)
                .append(']')
                .toString();
    }

    /** @see BlobStoreInfo#createInstance(TileLayerDispatcher, LockProvider) */
    @Override
    public BlobStore createInstance(TileLayerDispatcher layers, LockProvider lockProvider)
            throws StorageException {
        checkState(getName() != null, "id not set");
        checkState(
                isEnabled(),
                "Can't call LatestFileBlobStoreConfig.createInstance() is blob store is not enabled");
        checkState(baseDirectory != null, "baseDirectory not provided");
        checkState(match != null, "match not provided");
        checkState(replace != null, "replace not provided");
        LatestFileBlobStore fileBlobStore = new LatestFileBlobStore(baseDirectory);
        fileBlobStore.setMatch(match);
        fileBlobStore.setReplace(replace);

        return fileBlobStore;
    }
    /** @see BlobStoreInfo#getLocation() */
    @Override
    public String getLocation() {
        return getBaseDirectory();
    }
}