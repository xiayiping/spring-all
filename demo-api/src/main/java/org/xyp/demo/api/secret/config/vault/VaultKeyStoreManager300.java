package org.xyp.demo.api.secret.config.vault;

import org.xyp.demo.api.secret.config.InMemoryJksStoreDetails;
import org.xyp.demo.api.secret.config.KeyStoreManager300;
import org.xyp.demo.api.secret.config.StoreUtil;

import java.security.KeyStore;

public class VaultKeyStoreManager300 implements KeyStoreManager300 {

    private final VaultKeyStoreLoader storeLoader;

    public VaultKeyStoreManager300(VaultKeyStoreLoader storeLoader) {
        this.storeLoader = storeLoader;
    }

    @Override
    public KeyStore getKeyStore(String bundleName) {
        String keyPassword = storeLoader.loadKeyStorePassword(bundleName);
        byte[] keyStoreContent = storeLoader.loadKeyStore(bundleName);

        InMemoryJksStoreDetails keyStoreDetail =
                StoreUtil.getStoreDetails("key-" + bundleName,
                        keyStoreContent, keyPassword);
        KeyStore store = StoreUtil.createKeyStore(
                "key-" + bundleName, keyStoreDetail);

        return store;
    }

    @Override
    public KeyStore getTrustStore(String bundleName) {
        String trustPassword = storeLoader.loadTrustStorePassword(bundleName);
        byte[] trustStoreContent = storeLoader.loadTrustStore(bundleName);

        InMemoryJksStoreDetails trustStoreDetail =
                StoreUtil.getStoreDetails("trust-" + bundleName,
                        trustStoreContent, trustPassword);
        KeyStore store = StoreUtil.createKeyStore(
                "trust-" + bundleName, trustStoreDetail);

        return store;
    }

    @Override
    public String getKeyStorePassword(String bundleName) {
        return storeLoader.loadKeyStorePassword(bundleName);
    }

    @Override
    public String getTrustStorePassword(String bundleName) {
        return storeLoader.loadTrustStorePassword(bundleName);
    }
}
