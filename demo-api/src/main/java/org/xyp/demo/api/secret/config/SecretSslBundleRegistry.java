package org.xyp.demo.api.secret.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;

@Data
@Slf4j
public class SecretSslBundleRegistry extends DefaultSslBundleRegistry {

    private VaultKeyStoreSecretProperty vaultSecretProperty;

    public SecretSslBundleRegistry() {}

    public SecretSslBundleRegistry(String name, SslBundle bundle) {
        registerBundle(name, bundle);
    }

    @Override
    public void registerBundle(String name, SslBundle bundle) {
        log.info("register bundle " + name);
        super.registerBundle(name, bundle);
    }

    @Override
    public SslBundle getBundle(String bundleName) throws NoSuchSslBundleException {
        return super.getBundle(bundleName);
    }
}
