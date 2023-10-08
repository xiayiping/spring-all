package org.xyp.demo.httpclient.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
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
import java.util.Base64;
import java.util.Map;

class HttpClientTest {

    SSLContext createSSLContext() throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException {

        KeyStore ks1 = KeyStore.getInstance(new File("d:/tools/vault/1.14/vault-trust.p12"), "123456".toCharArray());
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//        tmf.init(ks1);

        return new SSLContextBuilder()
                .loadTrustMaterial(ks1, new TrustAllStrategy())
                .build();
    }

    SSLContext createSSLContextFromPem() throws Exception {

        val pemBytes = Files.readAllBytes(Path.of("d:/tools/vault/1.14/ca.pem"));
        val pemContent = new String(pemBytes);

        // Convert PEM to X509Certificate
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(pemBytes));

        // Create a new JKS and add the certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null); // the load null null is MUST
        trustStore.setCertificateEntry("alias", certificate);

//        KeyStore ks1 = KeyStore.getInstance(new File("d:/tools/vault/1.14/vault-trust.p12"), "123456".toCharArray());
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//        tmf.init(trustStore);

        return new SSLContextBuilder()
                .loadTrustMaterial(trustStore, new TrustAllStrategy())
                .build();
    }

    @Test
    void test1() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
//                .sslContext(createSSLContext())
                .sslContext(createSSLContextFromPem())
                .build();
//        curl\
//        -H "X-Vault-Request: true"\
//        -H "X-Vault-Token: hvs.6SnLsAKoGIyHE4sE5NtRvk0b"\
//        https://127.0.0.1:8180/v1/kv_xyp/data/dev

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8180/v1/kv_xyp/data/dev"))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", "hvs.CAESIH66nAoa6gU05CN1CIpKIpaP3pkNYM2gbMEjmo7szQ4WGh4KHGh2cy4wQlB3Z25tMnFFS2NodjhPZzhpak9XSkQ")
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
        Map<?, ?> m3 = (Map) m2.get("data");
        System.out.println("---- m3 ----");
        System.out.println(m3);
        System.out.println();
        Object m4 = m3.get("private_key");
        System.out.println(m4);
    }

    @Test
    void test2() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
//                .sslContext(createSSLContext())
                .sslContext(createSSLContextFromPem())
                .build();
//        curl\
//        -H "X-Vault-Request: true"\
//        -H "X-Vault-Token: hvs.6SnLsAKoGIyHE4sE5NtRvk0b"\
//        https://127.0.0.1:8180/v1/kv_xyp/data/dev

        String certKey = "09:d7:a4:5d:3c:df:bd:62:d3:42:a6:ca:e0:5e:23:39:89:5e:0e:3b";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://127.0.0.1:8180/v1/pki_xyp_int/cert/" + certKey))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", "hvs.CAESIH66nAoa6gU05CN1CIpKIpaP3pkNYM2gbMEjmo7szQ4WGh4KHGh2cy4wQlB3Z25tMnFFS2NodjhPZzhpak9XSkQ")
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
        Object m3 = m2.get("certificate");
        System.out.println("---- m3 ----");
        System.out.println(m3);
    }

    /*

    path "kv_xyp/*" {
        capabilities = ["read"]
    }


     */
    @Test
    void test3() throws Exception {
        HttpClient client = HttpClient.newBuilder()
//                .sslContext(createSSLContext())
                .build();
//        curl\
//        -H "X-Vault-Request: true"\
//        -H "X-Vault-Token: hvs.6SnLsAKoGIyHE4sE5NtRvk0b"\
//        https://127.0.0.1:8180/v1/kv_xyp/data/dev

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.google.com"))
//                .headers(
//                        "X-Vault-Request", "true",
//                        "X-Vault-Token", "hvs.6SnLsAKoGIyHE4sE5NtRvk0b")
//                .timeout(Duration.ofMillis(5009))
                .GET()
                .build();


        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(body.body());
    }

    @Test
    void testS() throws IOException {
        String path = "d:/develop/spring-all/demo-echo/src/main/resources/keystore/certstore.p12";
        val bytes = Files.readAllBytes(Path.of(path));
        System.out.println(Base64.getEncoder().encodeToString(bytes));
    }
}
