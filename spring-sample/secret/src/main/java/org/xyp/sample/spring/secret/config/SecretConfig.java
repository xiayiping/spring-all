package org.xyp.sample.spring.secret.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.xyp.sample.spring.secret.KeyStoreLoader;
import org.xyp.sample.spring.secret.SecretSslBundleRegistrar;

import java.util.List;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({KeyStoreSecretProperty.class})
public class SecretConfig {

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
