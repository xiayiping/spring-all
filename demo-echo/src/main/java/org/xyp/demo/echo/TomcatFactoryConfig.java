package org.xyp.demo.echo;

import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.ssl.*;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;


@Configuration
public class TomcatFactoryConfig {

    public TomcatFactoryConfig() {
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            System.out.println(factory);
            System.out.println(factory);
            System.out.println(factory);
            System.out.println(factory);
            System.out.println(factory);
        };
    }

    @Bean
    public TomcatConnectorCustomizer sslConnectorCustomizer(SslBundles sslBundles)
            throws Exception {
//        val sslBundle = sslBundles.getBundle("web-server-pem-fake");
        return connector -> {

            try {
                System.out.println("customize connector");
                ProtocolHandler handler = connector.getProtocolHandler();
                Assert.state(handler instanceof AbstractHttp11JsseProtocol,
                        "To use SSL, the connector's protocol handler must be an AbstractHttp11JsseProtocol subclass");

                // should be fetched from upstream
                var password = "123456";

                var keyStoreDetail = getStoreDetails("d:/develop/spring-all/demo-echo/src/main/resources/keystore/keystore.p12", password);
                var trustStoreDetail = getStoreDetails("d:/develop/spring-all/demo-echo/src/main/resources/keystore/certstore.p12", password);

                var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
                var sslBundle = new InMemoryPropertiesSslBundle(
                        sslStoreBundle, new JksSslBundleProperties());

                if (handler instanceof AbstractHttp11JsseProtocol<?> protocol) {
                    SslBundleKey key = sslBundle.getKey();
                    SslStoreBundle stores = sslBundle.getStores();
                    SslOptions options = sslBundle.getOptions();
                    protocol.setSSLEnabled(true);
                    SSLHostConfig sslHostConfig = new SSLHostConfig();
                    sslHostConfig.setHostName(protocol.getDefaultSSLHostConfigName());
                    sslHostConfig.setSslProtocol(sslBundle.getProtocol());
                    protocol.addSslHostConfig(sslHostConfig);
                    configureSslClientAuth(sslHostConfig);
                    SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.UNDEFINED);
                    String keystorePassword = (stores.getKeyStorePassword() != null) ? stores.getKeyStorePassword() : "";
                    certificate.setCertificateKeystorePassword(keystorePassword);
                    if (key.getPassword() != null) {
                        certificate.setCertificateKeyPassword(key.getPassword());
                    }
                    if (key.getAlias() != null) {
                        certificate.setCertificateKeyAlias(key.getAlias());
                    }
                    sslHostConfig.addCertificate(certificate);
                    if (options.getCiphers() != null) {
                        String ciphers = StringUtils.arrayToCommaDelimitedString(options.getCiphers());
                        sslHostConfig.setCiphers(ciphers);
                    }
                    configureEnabledProtocols(protocol, sslBundle);
                    configureSslStoreProvider(protocol, sslHostConfig, certificate, sslBundle);

                }
                connector.setScheme("https");
                connector.setSecure(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void configureSslClientAuth(SSLHostConfig config) {
//        config.setCertificateVerification(Ssl.ClientAuth.map(this.clientAuth, "none", "optional", "required"));
        config.setCertificateVerification("none");
    }

    private void configureSslStoreProvider(AbstractHttp11JsseProtocol<?> protocol,
                                           SSLHostConfig sslHostConfig,
                                           SSLHostConfigCertificate certificate,
                                           SslBundle sslBundle) {
        Assert.isInstanceOf(Http11NioProtocol.class, protocol,
                "SslStoreProvider can only be used with Http11NioProtocol");
        try {
            SslStoreBundle stores = sslBundle.getStores();
            if (stores.getKeyStore() != null) {
                certificate.setCertificateKeystore(stores.getKeyStore());
            }
            if (stores.getTrustStore() != null) {
                sslHostConfig.setTrustStore(stores.getTrustStore());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not load store: " + ex.getMessage(), ex);
        }
    }

    private void configureEnabledProtocols(AbstractHttp11JsseProtocol<?> protocol,
                                           SslBundle sslBundle) {
        SslOptions options = sslBundle.getOptions();
        if (options.getEnabledProtocols() != null) {
            String enabledProtocols = StringUtils.arrayToCommaDelimitedString(options.getEnabledProtocols());
            for (SSLHostConfig sslHostConfig : protocol.findSslHostConfigs()) {
                sslHostConfig.setProtocols(enabledProtocols);
            }
        }
    }

    private InMemoryJksStoreDetails getStoreDetails(String location, String password)
            throws URISyntaxException, IOException {
        var content = Files.readAllBytes(Path.of(location));
//        System.out.println(new String(content));
//        return new JksSslStoreDetails(null, null, new String(content), password);
        return new InMemoryJksStoreDetails(null, null, location, content, password);
    }

}
