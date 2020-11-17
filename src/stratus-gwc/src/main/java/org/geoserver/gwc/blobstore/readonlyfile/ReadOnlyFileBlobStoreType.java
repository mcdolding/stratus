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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.gwc.web.blob.BlobStoreType;

/**
 * Mirrors ReadOnlyFileBlobStoreType but for readonly use with block size removed.
 * @author mike.dolding
 */
public class ReadOnlyFileBlobStoreType implements BlobStoreType<ReadOnlyFileBlobStoreInfo> {
    private static final long serialVersionUID = 5634262173245722281L;

    @Override
    public String toString() {
        return "File BlobStore (readonly)";
    }

    @Override
    public ReadOnlyFileBlobStoreInfo newConfigObject() {
        ReadOnlyFileBlobStoreInfo config = new ReadOnlyFileBlobStoreInfo();
        config.setEnabled(true);
        return config;
    }

    @Override
    public Class<ReadOnlyFileBlobStoreInfo> getConfigClass() {
        return ReadOnlyFileBlobStoreInfo.class;
    }

    @Override
    public Panel createPanel(String id, IModel<ReadOnlyFileBlobStoreInfo> model) {
        return new ReadOnlyFileBlobStorePanel(id, model);
    }
}
