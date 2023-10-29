package org.xyp.demo.api.secret.config;

import lombok.val;
import org.springframework.boot.web.server.SslStoreProvider;
import org.xyp.demo.api.secret.config.vault.VaultKeyStoreManager300;

import java.security.KeyStore;

import static org.xyp.demo.api.secret.config.StoreUtil.getStoreDetails;
import static org.xyp.demo.api.secret.config.VaultUtil.*;

/**
 * only used by tomcat server
 */
@SuppressWarnings("removal")
public class SecretSslStoreProvider implements SslStoreProvider {

    private final KeyStoreManager300 storeManager;
    private final String bundleName;

    public SecretSslStoreProvider(
            KeyStoreManager300 storeManager,
            String bundleName
    ) {
        this.storeManager = storeManager;
        this.bundleName = bundleName;
    }

    @Override
    public KeyStore getKeyStore() {
        return storeManager.getKeyStore(this.bundleName);
    }

    @Override
    public KeyStore getTrustStore() {
        return storeManager.getTrustStore(this.bundleName);
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
