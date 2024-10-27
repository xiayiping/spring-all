package org.xyp.shared.secret.loader.cert;


import org.xyp.shared.secret.loader.cert.config.KeyStoreSecretProperty;

public interface KeyStoreLoader {

    boolean isSupported(KeyStoreSecretProperty.KeyStoreEntry entry);

    byte[] loadKeyStore(String bundleName);

    byte[] loadTrustStore(String bundleName);

    byte[] loadKeyStorePassword(String bundle);

    byte[] loadTrustStorePassword(String bundle);
}
