package org.xyp.demo.echo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;


@Configuration
public class TomcatFactoryConfig {

    public TomcatFactoryConfig() {
    }

    //    String vaultPemPath = "d:/tools/vault/1.14/ca.pem";
    String vaultPemPath = "d:/tools/vault/1.14/tcghl-com-crt.pem";

    //    String vaultRoot = "https://127.0.0.1:8180";
    String vaultRoot = "https://vault.tcghl.com";

    //    String vaultToken = "hvs.CAESIH66nAoa6gU05CN1CIpKIpaP3pkNYM2gbMEjmo7szQ4WGh4KHGh2cy4wQlB3Z25tMnFFS2NodjhPZzhpak9XSkQ";
    String vaultToken = "hvs.CAESIOuIuBjV-viraSq1zb6A7F5Aeg4icLbz9HyfEXTZMlaXGh4KHGh2cy5EWFNadDVOM1VtbEJkUWhIb1FpZjQ3QU4";

    //    String secretPath = vaultRoot + "/v1/kv_xyp/data/dev";
    String secretPath = vaultRoot + "/v1/secret/data/dev/paradise/keystore";

    //    String passwordField = "keystore_key";
    String passwordField = "password";

    //    String keyStoreField = "private_keystoe";
    String keyStoreField = "key_store";

    String trustStoreField = "trust_store";


    SSLContext createSSLContextFromPem() throws Exception {

        val pemBytes = Files.readAllBytes(Path.of(vaultPemPath));
        System.out.println("------------------ read vault pem from " + vaultPemPath);

        // Convert PEM to X509Certificate
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(pemBytes));

        // Create a new JKS and add the certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null); // the load null/null is MUST
        trustStore.setCertificateEntry("alias", certificate);

        return new SSLContextBuilder()
                .loadTrustMaterial(trustStore, new TrustAllStrategy())
                .build();
    }

    private byte[] getPrivateKeyFromVault() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
                .sslContext(createSSLContextFromPem())
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(5009))
                .build();

        System.out.println("=-==================== access secret via " + secretPath);

        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(body.body().getBytes(), Map.class);
        System.out.println(map.get("errors"));

        System.out.println("---- map ----");
        System.out.println(map);
        Map<?, ?> m2 = (Map) map.get("data");
        Map<?, ?> m3 = (Map) m2.get("data");
        Object m4 = m3.get(keyStoreField);
        System.out.println("---- private key store ----");
        System.out.println("[" + m4 + "]");

        return Base64.getDecoder().decode(m4.toString());//.getBytes();
    }

    private byte[] getCertificateFromVault() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
                .sslContext(createSSLContextFromPem())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(5009))
                .build();


        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(body.body());
        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(body.body().getBytes(), Map.class);
        System.out.println(map.get("errors"));

        Map<?, ?> m2 = (Map) map.get("data");
        Map<?, ?> m3 = (Map) m2.get("data");
        Object m4 = m3.get(trustStoreField);
        System.out.println("---- trust store ----");
        System.out.println(m4);

        return Base64.getDecoder().decode(m4.toString());
    }

    private String getKeyPasswordFromVault() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
                .sslContext(createSSLContextFromPem())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(5009))
                .build();


        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(body.body());
        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(body.body().getBytes(), Map.class);
        System.out.println(map.get("errors"));

        System.out.println("---- map ----");
        System.out.println(map);
        Map<?, ?> m2 = (Map) map.get("data");
        System.out.println("---- m2 ----");
        System.out.println(m2);
        System.out.println();
        Map m3 = (Map)m2.get("data");
        Object m4 = m3.get(passwordField);
        System.out.println("---- password ----");
        System.out.println(m4);

        return m4.toString();
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
    public TomcatConnectorCustomizer sslConnectorCustomizer()
            throws Exception {
//        val sslBundle = sslBundles.getBundle("web-server-pem-fake");
        return connector -> {

            try {
                System.out.println("customize connector");
                ProtocolHandler handler = connector.getProtocolHandler();
                Assert.state(handler instanceof AbstractHttp11JsseProtocol,
                        "To use SSL, the connector's protocol handler must be an AbstractHttp11JsseProtocol subclass");

                val password = getKeyPasswordFromVault();

                val privateKey = getPrivateKeyFromVault();
                var keyStoreDetail = getStoreDetails("keystore", privateKey, password);

                val trustCertificate = getCertificateFromVault();
                var trustStoreDetail = getStoreDetails("trustStore", trustCertificate, password);

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
        config.setCertificateVerification("required");
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

    private InMemoryJksStoreDetails getStoreDetails(String location, byte[] content, String password) {
        return new InMemoryJksStoreDetails(null, null, location, content, password);
    }


}
