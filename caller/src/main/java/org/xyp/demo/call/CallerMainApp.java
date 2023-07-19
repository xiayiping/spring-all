package org.xyp.demo.call;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.val;
import org.apache.tomcat.util.net.SSLContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

@SpringBootApplication
public class CallerMainApp {
    public static void main(String[] args) {
        SpringApplication.run(CallerMainApp.class, args);
    }

    @Bean
    public RestTemplate getRestTemplate(RestTemplateBuilder builder, SslBundles sslBundles) {
        return builder
                .setSslBundle(sslBundles.getBundle("clientStoreJks"))
                .build();
    }

    @Bean
    public WebClient getHttpClient(WebClientSsl ssl) throws Exception {
        return WebClient.builder().baseUrl("https://localhost:8092/echo")
                .apply(ssl.fromBundle("clientStoreJks"))
                .build();
    }

    public SslContext buildSslContext() throws Exception {
        KeyStore ks1 = KeyStore.getInstance("JKS");
        ks1.load(new FileInputStream("D:\\develop\\spring-all\\caller\\src\\main\\resources\\keystore\\client.keystore"), "123456".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks1);


        KeyStore ks2 = KeyStore.getInstance("JKS");
        ks2.load(new FileInputStream("D:\\develop\\spring-all\\caller\\src\\main\\resources\\keystore\\client.truststore"), "123456".toCharArray());
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks2, "123456".toCharArray());

        return SslContextBuilder.forClient()
                .trustManager(tmf)
                .keyManager(kmf)
                .build();
    }
}
