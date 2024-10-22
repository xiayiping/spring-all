package org.xyp.sample.spring.common.sslcert;

import lombok.AllArgsConstructor;
import org.springframework.boot.ssl.SslStoreBundle;
import org.xyp.sample.spring.common.sslcert.model.InMemoryJksStoreDetails;

import java.security.KeyStore;
import java.util.Optional;

@AllArgsConstructor
public class InMemoryJksSslStoreBundle implements SslStoreBundle {
    private final InMemoryJksStoreDetails inMemKeyStoreDetails;
    private final InMemoryJksStoreDetails inMemTrustStoreDetails;

    @Override
    public KeyStore getKeyStore() {
        return StoreUtil.createKeyStore("key", this.inMemKeyStoreDetails).orElse(null);
    }

    @Override
    public String getKeyStorePassword() {
        return Optional.ofNullable(inMemKeyStoreDetails)
            .map(InMemoryJksStoreDetails::password)
            .map(String::new)
            .orElse(null);
    }

    @Override
    public KeyStore getTrustStore() {
        return StoreUtil.createKeyStore("trust", this.inMemTrustStoreDetails).orElse(null);
    }
}
