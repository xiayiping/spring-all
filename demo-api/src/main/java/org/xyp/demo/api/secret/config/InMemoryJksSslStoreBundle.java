package org.xyp.demo.api.secret.config;

import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class InMemoryJksSslStoreBundle implements SslStoreBundle {

//    private final JksSslStoreDetails keyStoreDetails;
//
//    private final JksSslStoreDetails trustStoreDetails;

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
        return (this.inMemKeyStoreDetails != null) ? this.inMemKeyStoreDetails.password() : null;
    }

    @Override
    public KeyStore getTrustStore() {
        return StoreUtil.createKeyStore("trust", this.inMemTrustStoreDetails);
    }
}
