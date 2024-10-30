package org.xyp.shared.secret.loader.cert.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.xyp.shared.secret.loader.cert.KeyStoreLoader;
import org.xyp.shared.secret.loader.cert.SecretSslBundleRegistrar;
import org.xyp.shared.secret.loader.cert.vault.VaultKeyStoreLoader;
import org.xyp.shared.secret.vault.VaultClient;
import org.xyp.shared.secret.vault.VaultClientConfig;
import org.xyp.shared.secret.vault.VaultClientProperties;

import java.util.List;

@Slf4j
@Configuration
@Import(VaultClientConfig.class)
@EnableConfigurationProperties({KeyStoreSecretProperty.class, VaultClientProperties.class})
@ConditionalOnProperty(value = "org.xyp.shared.secret.keystore.enabled", matchIfMissing = true)
public class KeyStoreConfig {

    @Bean
    @ConditionalOnBean(VaultClient.class)
    VaultKeyStoreLoader vaultKeyStoreLoader(
        KeyStoreSecretProperty property,
        VaultClient vaultClient
    ) {
        val vault = new VaultKeyStoreLoader(vaultClient);
        vault.loadData(property);
        return vault;
    }

    @Bean
    @Order(Integer.MAX_VALUE)
    SecretSslBundleRegistrar secretSslBundleRegistry(
        SslBundleRegistrar baseSslBundleRegistrar,
        List<KeyStoreLoader> keyStoreLoader,
        KeyStoreSecretProperty property
    ) {
        return new SecretSslBundleRegistrar(baseSslBundleRegistrar, keyStoreLoader, property);
    }

}
