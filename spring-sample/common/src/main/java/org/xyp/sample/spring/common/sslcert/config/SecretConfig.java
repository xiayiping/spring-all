package org.xyp.sample.spring.common.sslcert.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;
import org.xyp.sample.spring.common.sslcert.KeyStoreLoader;
import org.xyp.sample.spring.common.sslcert.SecretSslBundleRegister;
import org.xyp.sample.spring.common.sslcert.vault.VaultKeyStoreLoader;

import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties({
    KeyStoreSecretProperty.class,
    VaultProperties.class
})
public class SecretConfig {

    @Bean
    @ConditionalOnProperty(value = "org.xyp.secret.keystore.enabled", matchIfMissing = true)
    VaultKeyStoreLoader vaultKeyStoreLoader(
        KeyStoreSecretProperty property,
        VaultProperties vaultProperties,
        VaultTemplate vaultTemplate
    ) {
        val vault = new VaultKeyStoreLoader(vaultTemplate, vaultProperties);
        vault.loadData(property);
        return vault;
    }

    @Bean
    @Order(Integer.MAX_VALUE)
    @ConditionalOnProperty(value = "org.xyp.secret.keystore.enabled", matchIfMissing = true)
    SecretSslBundleRegister secretSslBundleRegister(
        SslBundleRegistrar baseSslBundleRegistrar,
        List<KeyStoreLoader> keyStoreLoaders,
        KeyStoreSecretProperty property
    ) {
        return new SecretSslBundleRegister(baseSslBundleRegistrar, keyStoreLoaders, property);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.cloud.vault.enabled", matchIfMissing = false)
    VaultTemplateConfig vaultTemplateConfig(VaultProperties vaultProperties) {
        return new VaultTemplateConfig(vaultProperties);
    }
}
