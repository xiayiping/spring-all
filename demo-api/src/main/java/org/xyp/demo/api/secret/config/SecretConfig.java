package org.xyp.demo.api.secret.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xyp.demo.api.secret.config.vault.VaultKeyStoreLoader;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(VaultKeyStoreSecretProperty.class)
public class SecretConfig {
    public SecretConfig() {
    }

    @Configuration
    @EnableConfigurationProperties(VaultKeyStoreSecretProperty.class)
    @ConditionalOnClass(SslBundleRegistrar.class)
    public static class SpringBoot310Config {

        @Bean
        public SecretSslBundleRegistratar secretSslBundleRegistry() {
            return new SecretSslBundleRegistratar();
        }

    }

    @Configuration
    @EnableConfigurationProperties(VaultKeyStoreSecretProperty.class)
    @ConditionalOnMissingClass("org.springframework.boot.ssl.SslBundle")
    public static class SpringBoot300Config{

        @Bean
        public SecretSslStoreProvider secretSslStoreProvider() {
            log.info("create ssl store provider");
            return new SecretSslStoreProvider();
        }

        @Bean
        public SecretServletWebServerFactoryCustomizer customizer(
                ServerProperties serverProperties) {
            return new SecretServletWebServerFactoryCustomizer(
                    serverProperties, secretSslStoreProvider());
        }
    }

    @Bean
    public VaultKeyStoreLoader vaultKeyStoreLoader(VaultKeyStoreSecretProperty property) {
        return new VaultKeyStoreLoader(property);
    }

}
