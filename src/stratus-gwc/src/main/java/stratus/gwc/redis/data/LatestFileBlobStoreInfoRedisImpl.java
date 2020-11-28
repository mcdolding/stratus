/* (c) Planet Labs Inc. - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package stratus.gwc.redis.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.geoserver.gwc.blobstore.latest.LatestFileBlobStoreInfo;

/**
 * Based on FileBlobStoreInfoRedisImpl, except no block size
 * @author mike.dolding
 *
 */
@Data
@EqualsAndHashCode (callSuper = true)
public class LatestFileBlobStoreInfoRedisImpl extends BlobStoreInfoRedisImpl<LatestFileBlobStoreInfo> {

    /** serialVersionUID */
    private static final long serialVersionUID = 4435540163192060404L;

    private String match;

    private String replace;
    /**
     * Create from a real BlobStoreInfo
     */
    public LatestFileBlobStoreInfoRedisImpl(LatestFileBlobStoreInfo template) {
        super(template);
    }


    /**
     * Create from values
     */
    public LatestFileBlobStoreInfoRedisImpl(String name, boolean enabled, boolean default1,
                                            String baseDirectory, String match, String replace) {
        super(name, enabled, default1);
        this.baseDirectory = baseDirectory;
        this.match = match;
        this.replace = replace;
    }

    /**
     * Default constructor to make serializtion happy
     */
    public LatestFileBlobStoreInfoRedisImpl() {
        super();
    }
    
    private String baseDirectory;
    
    @Override
    protected LatestFileBlobStoreInfo constructInfo() {
        return new LatestFileBlobStoreInfo();
    }
}
