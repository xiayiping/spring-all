package org.xyp.shared.secret.vault;

import lombok.val;
import org.springframework.boot.autoconfigure.ssl.PemSslBundleProperties;
import org.springframework.boot.autoconfigure.ssl.PropertiesSslBundle;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.xyp.shared.secret.model.CertificateFileType;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class VaultClientRestImpl implements VaultClient {

    final String schema;
    final String host;
    final int port;
    final String url;

    final RestTemplate vaultRestTemplate;

    public VaultClientRestImpl(VaultClientProperties properties) {

        vaultRestTemplate = Optional.ofNullable(properties.getTrustStoreType())
            .filter(CertificateFileType.PEM::equals)
            .map(__ -> getPemSslBundleProperties(properties))
            .map(PropertiesSslBundle::get)

            .map(bundle -> new RestTemplateBuilder().setSslBundle(bundle))
            .or(() -> Optional.of(new RestTemplateBuilder()))

            .map(builder -> builder.additionalInterceptors((req, body, exec) -> {
                req.getHeaders().add("X-Vault-Token", properties.getToken());
                req.getHeaders().add("host", properties.getHost());
                return exec.execute(req, body);
            }))
            .map(RestTemplateBuilder::build)
            .orElseThrow(() -> new IllegalStateException("cannot create vault client rest template"))
        ;

        this.schema = properties.getSchema().name().toLowerCase();
        this.host = properties.getHost();
        this.port = properties.getPort();
        this.url = Optional.of(schema)
            .or(() -> Optional.of("http"))
            .map(s -> s + "://" + host + ":" + port)
            .get();
    }

    private PemSslBundleProperties getPemSslBundleProperties(VaultClientProperties properties) {
        try {
            PemSslBundleProperties pemSslBundleProperties = new PemSslBundleProperties();
            pemSslBundleProperties.getTruststore().setCertificate(
                properties.getTrustStore().getURL().getProtocol()
                    + ":" + properties.getTrustStore().getURL().getPath());
            return pemSslBundleProperties;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public VaultKvResponse readKvFromPath(String engine, String path) {
        val fullPath = url +
            "/v1/" +
            engine +
            "/data/" +
            path;
        return vaultRestTemplate.getForEntity(URI.create(
                    fullPath
                ),
                VaultKvResponse.class
            )
            .getBody()
            ;
    }
}
