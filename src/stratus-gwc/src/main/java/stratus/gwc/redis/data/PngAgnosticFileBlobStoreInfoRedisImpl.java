/* (c) Planet Labs Inc. - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package stratus.gwc.redis.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.geoserver.gwc.blobstore.pngagnostic.PngAgnosticFileBlobStoreInfo;
import org.geowebcache.config.FileBlobStoreInfo;

/**
 * Serializable representation of a FileBlobStoreInfo annotated for Spring Data
 * @author smithkm
 *
 */
@Data
@EqualsAndHashCode (callSuper = true)
public class PngAgnosticFileBlobStoreInfoRedisImpl extends BlobStoreInfoRedisImpl<PngAgnosticFileBlobStoreInfo> {

    /** serialVersionUID */
    private static final long serialVersionUID = 4435540163192060403L;

    /**
     * Create from a real BlobStoreInfo
     */
    public PngAgnosticFileBlobStoreInfoRedisImpl(PngAgnosticFileBlobStoreInfo template) {
        super(template);
    }


    /**
     * Create from values
     */
    public PngAgnosticFileBlobStoreInfoRedisImpl(String name, boolean enabled, boolean default1,
                                                 String baseDirectory, int fileSystemBlockSize) {
        super(name, enabled, default1);
        this.baseDirectory = baseDirectory;
        this.fileSystemBlockSize = fileSystemBlockSize;
    }

    /**
     * Default constructor to make serializtion happy
     */
    public PngAgnosticFileBlobStoreInfoRedisImpl() {
        super();
    }
    
    private String baseDirectory;
    
    private int fileSystemBlockSize;
    
    @Override
    protected PngAgnosticFileBlobStoreInfo constructInfo() {
        return new PngAgnosticFileBlobStoreInfo();
    }
}
