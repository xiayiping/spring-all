package org.xyp.sample.spring.secret;


import org.xyp.sample.spring.secret.config.KeyStoreSecretProperty;

public interface KeyStoreLoader {

    boolean isSupported(KeyStoreSecretProperty.KeyStoreEntry entry);

    byte[] loadKeyStore(String bundleName);

    byte[] loadTrustStore(String bundleName);

    byte[] loadKeyStorePassword(String bundle);

    byte[] loadTrustStorePassword(String bundle);
}
