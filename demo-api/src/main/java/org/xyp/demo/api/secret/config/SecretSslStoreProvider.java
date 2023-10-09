package org.xyp.demo.api.secret.config;

import lombok.val;
import org.springframework.boot.web.server.SslStoreProvider;

import java.security.KeyStore;

import static org.xyp.demo.api.secret.config.StoreUtil.getStoreDetails;
import static org.xyp.demo.api.secret.config.VaultUtil.*;

public class SecretSslStoreProvider implements SslStoreProvider {
    @Override
    public KeyStore getKeyStore() throws Exception {

        String password = getKeyPasswordFromVault();
        val privateKey = getPrivateKeyFromVault();
        var keyStoreDetail = getStoreDetails("keystore", privateKey, password);
        var store =
                StoreUtil.createKeyStore("keystore", keyStoreDetail);
        return store;
    }

    @Override
    public KeyStore getTrustStore() throws Exception {
        String password = getKeyPasswordFromVault();
        val trustCertificate = getCertificateFromVault();
        var trustStoreDetail = getStoreDetails("trustStore", trustCertificate, password);

        var store =
                StoreUtil.createKeyStore("truststore", trustStoreDetail);
        return store;
    }

    @Override
    public String getKeyPassword() {
        try {
            return getKeyPasswordFromVault();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
