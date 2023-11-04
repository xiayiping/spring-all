package org.xyp.demo.api.secret.config;

public interface KeyStoreLoader {

    byte[] loadKeyStore(String bundleName);

    byte[] loadTrustStore(String bundleName);

    String loadKeyStorePassword(String bundle);

    String loadTrustStorePassword(String bundle);
}
