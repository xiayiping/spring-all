package org.xyp.sample.spring.common.sslcert.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.VaultToken;

import java.util.Objects;
import java.util.Optional;

@Slf4j
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
            .map(ssl -> ssl.withTrustStore(SslConfiguration.KeyStoreConfiguration
                .of(trustStoreOpt.get(), passwordOpt.get().toCharArray())
            ));
    }

    private SslConfiguration sslConfigurationByType(VaultProperties vaultProperties) {
        val typeOpt = Optional.of(vaultProperties.getSsl())
            .map(VaultProperties.Ssl::getTrustStoreType);
        val trustStoreOpt = getTrustStore(vaultProperties)
            .filter(ignored -> typeOpt.isPresent())
            .map(store -> store.withStoreType(typeOpt.get()));
        return typeOpt
            .filter(ignored -> trustStoreOpt.isPresent())
            .map(ignored -> SslConfiguration.unconfigured())
            .map(ssl -> ssl.withTrustStore(trustStoreOpt.get()))
            .orElseGet(SslConfiguration::unconfigured);
    }

    private Optional<SslConfiguration.KeyStoreConfiguration> getTrustStore(VaultProperties vaultProperties) {
        return Optional.of(vaultProperties.getSsl())
            .map(VaultProperties.Ssl::getTrustStore)
            .map(SslConfiguration.KeyStoreConfiguration::of);
    }
}
