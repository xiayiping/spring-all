package org.xyp.demo.api.secret.config;

import org.springframework.boot.ssl.SslBundle;

import java.security.KeyStore;

public interface KeyStoreManager extends KeyStoreManager300 {

    SslBundle getBundle(String bundleName);

}
