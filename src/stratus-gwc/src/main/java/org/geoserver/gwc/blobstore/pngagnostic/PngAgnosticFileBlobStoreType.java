/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.blobstore.pngagnostic;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.gwc.web.blob.BlobStoreType;
import org.geoserver.gwc.web.blob.FileBlobStorePanel;
import org.geoserver.gwc.web.blob.FileBlobStoreType;
import org.geowebcache.config.FileBlobStoreInfo;

public class PngAgnosticFileBlobStoreType implements BlobStoreType<PngAgnosticFileBlobStoreInfo> {
    private static final long serialVersionUID = 6825505034831901062L;

    @Override
    public String toString() {
        return "Png Agnostic File BlobStore";
    }

    @Override
    public PngAgnosticFileBlobStoreInfo newConfigObject() {
        PngAgnosticFileBlobStoreInfo config = new PngAgnosticFileBlobStoreInfo();
        config.setEnabled(true);
        config.setFileSystemBlockSize(4096);
        return config;
    }

    @Override
    public Class<PngAgnosticFileBlobStoreInfo> getConfigClass() {
        return PngAgnosticFileBlobStoreInfo.class;
    }

    @Override
    public Panel createPanel(String id, IModel<PngAgnosticFileBlobStoreInfo> model) {
        return new PngAgnosticFileBlobStorePanel(id, model);
    }
}
