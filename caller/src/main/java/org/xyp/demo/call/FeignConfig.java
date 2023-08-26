package org.xyp.demo.call;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.hc5.ApacheHttp5Client;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@EnableFeignClients
@ImportAutoConfiguration(FeignAutoConfiguration.class)
//@LoadBalancerClients({
//    @LoadBalancerClient("echoServer")
//})
public class FeignConfig {

    @Value("${echo.url}")
    String echoUrl;

    @Value("${server.ssl.bundle}")
    String sslBundle;

    public FeignConfig() {
        log.info("FeignConfig -------------");
    }

//    @Bean
//    public Client feignClient(SslBundles sslBundles) {
//        // add this bean, the load balancer won't work
//        log.info("Feign client create -------------");
//        val sslContext = sslBundles.getBundle(sslBundle).createSslContext();
//
//        FeignClientFactory context = new FeignClientFactory();
//
//        return new Client.Default(
////            sslContext.getSocketFactory(),
//            null,
//            new NoopHostnameVerifier());
//
//    }

//    @Bean
//    public Feign.Builder feignBuilder(SslBundles sslBundles) {
//        log.info("create Feign client Builder -------------");
//        val sslContext = sslBundles.getBundle(sslBundle).createSslContext();
//        SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactoryBuilder.create()
//            .setSslContext(sslContext).build();
//        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
//            .setSSLSocketFactory(sslConnectionSocketFactory).build();
//        return Feign.builder()
//            .retryer(Retryer.NEVER_RETRY)
//            .client(new ApacheHttp5Client(HttpClients.custom()
//                .setConnectionManager(connectionManager)
//                .build()));
//    }
}
