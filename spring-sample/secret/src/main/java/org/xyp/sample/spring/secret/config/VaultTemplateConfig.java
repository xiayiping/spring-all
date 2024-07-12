package org.xyp.sample.spring.secret.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.cloud.vault.config.VaultProperties.Ssl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.SslConfiguration.KeyStoreConfiguration;
import org.springframework.vault.support.VaultToken;
import org.xyp.sample.spring.secret.vault.VaultKeyStoreLoader;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Configuration
@ConditionalOnProperty("spring.cloud.vault.enabled")
public class VaultTemplateConfig extends AbstractVaultConfiguration {

    private final VaultProperties vaultProperties;

    public VaultTemplateConfig(VaultProperties vaultProperties) {
        log.info("create secret manager ... ...");
        this.vaultProperties = vaultProperties;
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        return VaultEndpoint.create(vaultProperties.getHost(), vaultProperties.getPort());
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        return () -> VaultToken.of(Objects.requireNonNull(vaultProperties.getToken()));
    }

    @Override
    public SslConfiguration sslConfiguration() {
        return sslConfigurationByPassword(vaultProperties)
            .orElseGet(() -> sslConfigurationByType(vaultProperties));
    }

    private Optional<SslConfiguration> sslConfigurationByPassword(VaultProperties vaultProperties) {
        val trustStoreOpt = Optional.ofNullable(vaultProperties.getSsl().getTrustStore());
        val passwordOpt = Optional.ofNullable(vaultProperties.getSsl().getTrustStorePassword());
        return trustStoreOpt
            .filter(ignored -> passwordOpt.isPresent())
            .map(password -> SslConfiguration.unconfigured())
            .map(ssl -> ssl.withTrustStore(KeyStoreConfiguration
                .of(trustStoreOpt.get(), passwordOpt.get().toCharArray())));
    }

    private SslConfiguration sslConfigurationByType(VaultProperties vaultProperties) {
        val typeOpt = Optional.of(vaultProperties.getSsl())
            .map(Ssl::getTrustStoreType);
        val trustStoreOpt = getTrustStore(vaultProperties)
            .filter(ignored -> typeOpt.isPresent())
            .map(store -> store.withStoreType(typeOpt.get()));
        return typeOpt
            .filter(ignored -> trustStoreOpt.isPresent())
            .map(ignored -> SslConfiguration.unconfigured())
            .map(ssl -> ssl.withTrustStore(trustStoreOpt.get()))
            .orElseGet(SslConfiguration::unconfigured);
    }

    private Optional<KeyStoreConfiguration> getTrustStore(VaultProperties vaultProperties) {
        return Optional.of(vaultProperties.getSsl())
            .map(Ssl::getTrustStore)
            .map(KeyStoreConfiguration::of);
    }

    @Bean
    VaultKeyStoreLoader vaultKeyStoreLoader(
        KeyStoreSecretProperty property,
        VaultProperties vaultProperties,
        VaultTemplate vaultTemplate
    ) {
        val vault = new VaultKeyStoreLoader(vaultTemplate, vaultProperties);
        vault.loadData(property);
        return vault;
    }

}
