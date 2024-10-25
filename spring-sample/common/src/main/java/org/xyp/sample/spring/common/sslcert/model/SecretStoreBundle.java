package org.xyp.sample.spring.common.sslcert.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SecretStoreBundle {
    byte[] keyStore;
    byte[] trustStore;
    byte[] keyPassword;
    byte[] trustPassword;

    public byte[] keyStore() {
        return keyStore;
    }

    public byte[] trustStore() {
        return trustStore;
    }

    public byte[] keyPassword() {
        return keyPassword;
    }

    public byte[] trustPassword() {
        return trustPassword;
    }
}
