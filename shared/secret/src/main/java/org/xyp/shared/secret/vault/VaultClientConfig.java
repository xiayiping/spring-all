package org.xyp.shared.secret.vault;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(VaultClientProperties.class)
public class VaultClientConfig {

    public VaultClientConfig() {
        log.info("vault client config ... ...");
    }

    @Bean
    @ConditionalOnProperty(name = VaultClientProperties.PREFIX + ".host", matchIfMissing = false)
    public VaultClient vaultClient(final VaultClientProperties properties) {
        log.info("creating vault client ... ...");
        return new VaultClientRestImpl(properties);
    }
}
