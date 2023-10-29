package org.xyp.demo.api.secret.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Optional;


@Slf4j
@Data
public class VaultClient {
    private String url;
    private String vaultPemPath;
//    private String secretPath;
    private String vaultToken;

    private HttpClient client = null;

//    public VaultClient(String url, String token, String vaultPemPath, String secretPath)
//            throws Exception {
//        this.url = url;
//        this.vaultPemPath = vaultPemPath;
//        this.secretPath = secretPath;
//        this.vaultToken = token;
//        init();
//    }

    public VaultClient(String url, String token, String vaultPemPath)
            throws Exception {
        this.url = url;
        this.vaultPemPath = vaultPemPath;
        this.vaultToken = token;
        init();
    }

    public String read(String secretPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(8000))
                .build();

        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("vault returned body " + body);
        log.debug("vault returned body " + body.body());
        return body.body();
    }

//    public String read() throws IOException, InterruptedException {
//        return read(this.secretPath);
//    }

    private void init() throws Exception {
        this.client = createClient();
    }

    private HttpClient createClient() throws Exception {
        return createSSLContextFromPem(this.vaultPemPath)
                .map(ctx -> HttpClient.newBuilder()
                        .sslContext(ctx)
                        .build())
                .orElse(HttpClient.newBuilder().build());

    }

    private Optional<SSLContext> createSSLContextFromPem(String vaultPemPath)
            throws IOException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        if (!StringUtils.hasText(vaultPemPath)) {
            return Optional.empty();
        }
        val pemBytes = Files.readAllBytes(Path.of(vaultPemPath));
        log.info("read vault pem from " + vaultPemPath);

        // Convert PEM to X509Certificate
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)
                certificateFactory.generateCertificate(new ByteArrayInputStream(pemBytes));

        // Create a new JKS and add the certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null); // the load null/null is MUST
        trustStore.setCertificateEntry("vaultCA", certificate);

        return Optional.of(new SSLContextBuilder()
                .loadTrustMaterial(trustStore, new TrustAllStrategy())
                .build());
    }
}
