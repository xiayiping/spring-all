package org.xyp.demo.api.secret.config;

import java.security.KeyStore;

public interface KeyStoreManager300 {

    KeyStore getKeyStore(String bundleName);

    KeyStore getTrustStore(String bundleName);

    String getKeyStorePassword(String bundleName);

    String getTrustStorePassword(String bundleName);
}
