package org.xyp.demo.call;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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
}
