package org.xyp.demo.api.secret.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.net.URI;
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
import java.util.Optional;

import static org.xyp.demo.api.secret.config.TempConst.*;

public class VaultUtil {

    public static SSLContext createSSLContextFromPem() throws Exception {
        return createSSLContextFromPem(vaultPemPath);
    }

    public static SSLContext createSSLContextFromPem(String vaultPemPath) throws Exception {

        if (!StringUtils.hasText(vaultPemPath)) {
            return null;
        }
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

    public static byte[] getPrivateKeyFromVault() throws Exception {
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

    public static byte[] getCertificateFromVault() throws Exception {
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

    public static String getKeyPasswordFromVault() throws Exception {
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
        Map m3 = (Map) m2.get("data");
        Object m4 = m3.get(passwordField);
        System.out.println("---- password ----");
        System.out.println(m4);

        return m4.toString();
    }

}
