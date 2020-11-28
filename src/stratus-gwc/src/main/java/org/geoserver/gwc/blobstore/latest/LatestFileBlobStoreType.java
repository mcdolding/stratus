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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.gwc.web.blob.BlobStoreType;

/**
 * Mirrors LatestFileBlobStoreType but for readonly use with block size removed.
 * @author mike.dolding
 */
public class LatestFileBlobStoreType implements BlobStoreType<LatestFileBlobStoreInfo> {
    private static final long serialVersionUID = 5634262173245722281L;

    @Override
    public String toString() {
        return "File BlobStore (latest)";
    }

    @Override
    public LatestFileBlobStoreInfo newConfigObject() {
        LatestFileBlobStoreInfo config = new LatestFileBlobStoreInfo();
        config.setEnabled(true);
        config.setMatch("LATEST");
        return config;
    }

    @Override
    public Class<LatestFileBlobStoreInfo> getConfigClass() {
        return LatestFileBlobStoreInfo.class;
    }

    @Override
    public Panel createPanel(String id, IModel<LatestFileBlobStoreInfo> model) {
        return new LatestFileBlobStorePanel(id, model);
    }
}
