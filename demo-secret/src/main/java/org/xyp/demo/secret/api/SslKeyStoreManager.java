package org.xyp.demo.secret.api;

import java.security.KeyStore;
import java.util.Map;

public interface SslKeyStoreManager {

    Map<String, KeyStore> getAllKeyStore();

    KeyStore getKeyStore(String key);
}
