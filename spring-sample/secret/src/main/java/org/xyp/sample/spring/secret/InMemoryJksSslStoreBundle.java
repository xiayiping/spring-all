package org.xyp.sample.spring.secret;

import org.springframework.boot.ssl.SslStoreBundle;
import org.xyp.sample.spring.secret.pojo.InMemoryJksStoreDetails;

import java.security.KeyStore;

public class InMemoryJksSslStoreBundle implements SslStoreBundle {

    private final InMemoryJksStoreDetails inMemKeyStoreDetails;
    private final InMemoryJksStoreDetails inMemTrustStoreDetails;

    /**
     * Location in details is the content of file
     *
     * @param keyStoreDetails   the key store details
     * @param trustStoreDetails the trust store details
     */
    public InMemoryJksSslStoreBundle(InMemoryJksStoreDetails keyStoreDetails, InMemoryJksStoreDetails trustStoreDetails) {
        this.inMemKeyStoreDetails = keyStoreDetails;
        this.inMemTrustStoreDetails = trustStoreDetails;

    }

    @Override
    public KeyStore getKeyStore() {
        return StoreUtil.createKeyStore("key", this.inMemKeyStoreDetails);
    }

    @Override
    public String getKeyStorePassword() {
        return (this.inMemKeyStoreDetails != null) ? new String(this.inMemKeyStoreDetails.password()) : null;
    }

    @Override
    public KeyStore getTrustStore() {
        return StoreUtil.createKeyStore("trust", this.inMemTrustStoreDetails);
    }
}
