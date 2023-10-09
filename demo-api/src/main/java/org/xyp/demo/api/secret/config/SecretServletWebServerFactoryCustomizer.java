package org.xyp.demo.api.secret.config;

import lombok.val;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.core.Ordered;

public class SecretServletWebServerFactoryCustomizer
        implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>, Ordered {

    private final ServerProperties serverProperties;

    private final SecretSslStoreProvider provider;

    public SecretServletWebServerFactoryCustomizer(ServerProperties serverProperties,
                                                   SecretSslStoreProvider provider) {
        this.serverProperties = serverProperties;
        this.provider = provider;
    }

    @Override
    @SuppressWarnings({"removal", "deprecation"})
    public void customize(ConfigurableServletWebServerFactory factory) {

        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        val ssl = this.serverProperties.getSsl();
        ssl.setKeyStorePassword(this.provider.getKeyPassword());

        factory.setSslStoreProvider(provider);
        factory.setSsl(ssl);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
