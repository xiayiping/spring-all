package org.xyp.demo.call;

import org.springframework.boot.autoconfigure.ssl.SslBundleProperties;
import org.springframework.boot.ssl.*;

public class InMemoryPropertiesSslBundle implements SslBundle {
    private final SslStoreBundle stores;
    private final SslBundleKey key;
    private final SslOptions options;
    private final String protocol;
    private final SslManagerBundle managers;

    public InMemoryPropertiesSslBundle(SslStoreBundle stores, SslBundleProperties properties) {
        this.stores = stores;
        this.key = asSslKeyReference(properties.getKey());
        this.options = asSslOptions(properties.getOptions());
        this.protocol = properties.getProtocol();
        this.managers = SslManagerBundle.from(this.stores, this.key);
    }

    private static SslBundleKey asSslKeyReference(SslBundleProperties.Key key) {
        return key != null ? SslBundleKey.of(key.getPassword(), key.getAlias()) : SslBundleKey.NONE;
    }

    private static SslOptions asSslOptions(SslBundleProperties.Options options) {
        return options != null ? SslOptions.of(options.getCiphers(), options.getEnabledProtocols()) : SslOptions.NONE;
    }

    @Override
    public SslStoreBundle getStores() {
        return this.stores;
    }

    @Override
    public SslBundleKey getKey() {
        return this.key;
    }

    @Override
    public SslOptions getOptions() {
        return this.options;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public SslManagerBundle getManagers() {
        return this.managers;
    }

}
