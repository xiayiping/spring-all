package org.xyp.demo.call;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
//@EnableFeignClients
//@EnableDiscoveryClient
//@LoadBalancerClients({
//    @LoadBalancerClient("echoServer")
//})
public class CallerMainApp {
    public static void main(String[] args) {
        SpringApplication.run(CallerMainApp.class, args);
    }

    @Value("${echo.url}")
    String echoUrl;

    @Value("${server.ssl.bundle}")
    String sslBundleKey;

    private boolean isHttps() {
        return echoUrl.startsWith("https");
    }

    @Bean
    public RestTemplate getRestTemplate(RestTemplateBuilder builder, SslBundles sslBundles) {
        return Optional.of(isHttps()).filter(i -> i)
            .map(i -> builder
                .rootUri(echoUrl)
                .setSslBundle(sslBundles.getBundle(sslBundleKey))
                .build())
            .orElseGet(() -> builder
                .rootUri(echoUrl)
                .build());
    }

    @Bean
    public WebClient getHttpClient(WebClientSsl ssl) throws Exception {
        val b = WebClient.builder().baseUrl(echoUrl);
        if (isHttps())
            return b.apply(ssl.fromBundle(sslBundleKey))
                .build();
        else return b.build();
    }
}
