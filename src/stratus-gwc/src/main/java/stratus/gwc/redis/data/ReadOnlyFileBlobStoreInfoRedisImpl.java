/* (c) Planet Labs Inc. - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package stratus.gwc.redis.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.geoserver.gwc.blobstore.readonlyfile.ReadOnlyFileBlobStoreInfo;

/**
 * Based on FileBlobStoreInfoRedisImpl, except no block size
 * @author mike.dolding
 *
 */
@Data
@EqualsAndHashCode (callSuper = true)
public class ReadOnlyFileBlobStoreInfoRedisImpl extends BlobStoreInfoRedisImpl<ReadOnlyFileBlobStoreInfo> {

    /** serialVersionUID */
    private static final long serialVersionUID = 4435540163192060404L;

    /**
     * Create from a real BlobStoreInfo
     */
    public ReadOnlyFileBlobStoreInfoRedisImpl(ReadOnlyFileBlobStoreInfo template) {
        super(template);
    }


    /**
     * Create from values
     */
    public ReadOnlyFileBlobStoreInfoRedisImpl(String name, boolean enabled, boolean default1,
                                              String baseDirectory) {
        super(name, enabled, default1);
        this.baseDirectory = baseDirectory;
    }

    /**
     * Default constructor to make serializtion happy
     */
    public ReadOnlyFileBlobStoreInfoRedisImpl() {
        super();
    }
    
    private String baseDirectory;
    
    @Override
    protected ReadOnlyFileBlobStoreInfo constructInfo() {
        return new ReadOnlyFileBlobStoreInfo();
    }
}
