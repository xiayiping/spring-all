package org.xyp.sample.spring.common.sslcert;

import org.xyp.sample.spring.common.sslcert.config.KeyStoreSecretProperty;

public interface KeyStoreLoader {
    boolean isSupported(KeyStoreSecretProperty.KeyStoreEntry entry);

    byte[] loadKeyStore(String bundleName);

    byte[] loadTrustStore(String bundleName);

    byte[] loadKeyStorePassword(String bundleName);

    byte[] loadTrustStorePassword(String bundleName);
}
