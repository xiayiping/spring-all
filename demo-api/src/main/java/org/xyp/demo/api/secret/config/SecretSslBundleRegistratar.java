package org.xyp.demo.api.secret.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.SslBundleRegistry;

import static org.xyp.demo.api.secret.config.VaultUtil.*;
import static org.xyp.demo.api.secret.config.StoreUtil.*;

@Slf4j
public class SecretSslBundleRegistratar implements SslBundleRegistrar {

    @Override
    public void registerBundles(SslBundleRegistry registry) {
        log.info("starting registration secret ssl bundles");

        try {
            String password = getKeyPasswordFromVault();
            val privateKey = getPrivateKeyFromVault();
            var keyStoreDetail = getStoreDetails("keystore", privateKey, password);

            val trustCertificate = getCertificateFromVault();
            var trustStoreDetail = getStoreDetails("trustStore", trustCertificate, password);

            var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
            var sslBundle = new InMemoryPropertiesSslBundle(
                    sslStoreBundle, new JksSslBundleProperties());

            registry.registerBundle("secretBundle", sslBundle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
