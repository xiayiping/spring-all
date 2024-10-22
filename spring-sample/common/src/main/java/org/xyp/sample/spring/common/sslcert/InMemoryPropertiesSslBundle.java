package org.xyp.sample.spring.common.sslcert;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.ssl.SslBundleProperties;
import org.springframework.boot.ssl.*;

import java.util.Optional;

public class InMemoryPropertiesSslBundle implements SslBundle {
    private final SslStoreBundle stores;
    private final SslBundleKey key;
    private final SslOptions options;
    private final String protocol;
    private final SslManagerBundle managers;

    public InMemoryPropertiesSslBundle(
        final SslStoreBundle stores,
        final SslBundleProperties properties
    ) {
        this.stores = stores;
        this.key = asSskKeyReference(properties.getKey());
        this.options = asSslOptions(properties.getOptions());
        this.protocol = properties.getProtocol();
        this.managers = SslManagerBundle.from(this.stores, this.key);
    }

    private static SslBundleKey asSskKeyReference(SslBundleProperties.Key key) {
        return Optional.ofNullable(key)
            .map(k -> SslBundleKey.of(k.getPassword(), k.getAlias()))
            .orElse(null);
    }

    private static SslOptions asSslOptions(SslBundleProperties.Options options) {
        return Optional.ofNullable(options)
            .map(o -> SslOptions.of(o.getCiphers(), o.getEnabledProtocols()))
            .orElse(SslOptions.NONE);
    }

    @Override
    public SslStoreBundle getStores() {
        return stores;
    }

    @Override
    public SslBundleKey getKey() {
        return key;
    }

    @Override
    public SslOptions getOptions() {
        return options;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public SslManagerBundle getManagers() {
        return managers;
    }
}
