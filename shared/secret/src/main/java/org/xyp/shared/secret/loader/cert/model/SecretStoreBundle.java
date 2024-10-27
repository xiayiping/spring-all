package org.xyp.shared.secret.loader.cert.model;

import lombok.Getter;

@Getter
public class SecretStoreBundle {
    byte[] keyStore;
    byte[] trustStore;
    byte[] keyPassword;
    byte[] trustPassword;

    public SecretStoreBundle(byte[] keyStore, byte[] trustStore,
                             byte[] keyPassword, byte[] trustPassword) {
        this.keyStore = keyStore;
        this.trustStore = trustStore;
        this.keyPassword = keyPassword;
        this.trustPassword = trustPassword;
    }

    public byte[] keyStore() {return keyStore;
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
