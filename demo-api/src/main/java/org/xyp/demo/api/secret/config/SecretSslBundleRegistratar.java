package org.xyp.demo.api.secret.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.xyp.demo.api.secret.config.vault.VaultKeyStoreLoader;

import java.util.Map;
import java.util.stream.Collectors;

import static org.xyp.demo.api.secret.config.VaultUtil.*;
import static org.xyp.demo.api.secret.config.StoreUtil.*;

@Slf4j
public class SecretSslBundleRegistratar implements SslBundleRegistrar {

    private final VaultKeyStoreLoader vaultKeyStoreLoader;
    private final VaultKeyStoreSecretProperty property;

    SecretSslBundleRegistratar(VaultKeyStoreLoader vaultKeyStoreLoader,
                               VaultKeyStoreSecretProperty property) {
        this.vaultKeyStoreLoader = vaultKeyStoreLoader;
        this.property = property;
    }

    @Override
    public void registerBundles(SslBundleRegistry registry) {
        log.info("starting registration secret ssl bundles");

        val vaultEntries = property.getEntries().entrySet().stream()
                .filter(e -> VaultKeyStoreLoader.SupportedType.equals(e.getValue().getType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, VaultKeyStoreSecretProperty.VaultSecurityEntry> ent : vaultEntries.entrySet()) {
            val bundles = ent.getValue().getPaths().values().stream()
                    .flatMap(p -> p.getBundles().entrySet().stream()).toList();
            for (Map.Entry<String, VaultKeyStoreSecretProperty.Bundle> bd : bundles) {

                String keyPassword = vaultKeyStoreLoader.loadKeyStorePassword(bd.getKey());
                String trustPassword = vaultKeyStoreLoader.loadTrustStorePassword(bd.getKey());
                byte[] keyStoreContent = vaultKeyStoreLoader.loadKeyStore(bd.getKey());
                byte[] trustStoreContent = vaultKeyStoreLoader.loadTrustStore(bd.getKey());
                var keyStoreDetail = getStoreDetails(
                        ent.getKey() + "-" + bd.getKey() + "-key"
                        , keyStoreContent, keyPassword);
                var trustStoreDetail = getStoreDetails(
                        ent.getKey() + "-" + bd.getKey() + "-trust"
                        , trustStoreContent, trustPassword);

                var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
                var sslBundle = new InMemoryPropertiesSslBundle(
                        sslStoreBundle, new JksSslBundleProperties());
                registry.registerBundle(bd.getKey(), sslBundle);
            }
        }
//        try {
//            String password = getKeyPasswordFromVault();
//            val privateKey = getPrivateKeyFromVault();
//            var keyStoreDetail = getStoreDetails("keystore", privateKey, password);
//
//            val trustCertificate = getCertificateFromVault();
//            var trustStoreDetail = getStoreDetails("trustStore", trustCertificate, password);
//
//            var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
//            var sslBundle = new InMemoryPropertiesSslBundle(
//                    sslStoreBundle, new JksSslBundleProperties());
//
//            registry.registerBundle("secretBundle", sslBundle);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    }
}
