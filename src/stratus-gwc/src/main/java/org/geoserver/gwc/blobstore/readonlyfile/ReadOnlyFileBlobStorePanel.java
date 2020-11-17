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

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.web.data.store.panel.DirectoryParamPanel;
import org.geoserver.web.wicket.ParamResourceModel;
/**
 * Mirrors ReadOnlyFileBlobStorePanel but for readonly use with block size removed.
 * @author mike.dolding
 */
public class ReadOnlyFileBlobStorePanel extends Panel  {

    private static final long serialVersionUID = -3843873237066531610L;

    public ReadOnlyFileBlobStorePanel(String id, final IModel<ReadOnlyFileBlobStoreInfo> configModel) {
        super(id, configModel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onInitialize() {
        super.onInitialize();

        DirectoryParamPanel paramPanel;
        add(
                paramPanel =
                        new DirectoryParamPanel(
                                "baseDirectory",
                                new PropertyModel<String>(
                                        getDefaultModel().getObject(), "baseDirectory"),
                                new ParamResourceModel("baseDirectory", this),
                                true));
        paramPanel.add(new AttributeModifier("title", new ResourceModel("baseDirectory.title")));
        paramPanel
                .getFormComponent()
                .setModel((IModel<String>) paramPanel.getDefaultModel()); // disable filemodel
        paramPanel.setFileFilter(
                new Model<DirectoryFileFilter>((DirectoryFileFilter) DirectoryFileFilter.INSTANCE));
    }
}
