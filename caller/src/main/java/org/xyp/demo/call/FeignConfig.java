package org.xyp.demo.call;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.hc5.ApacheHttp5Client;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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

import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
@EnableFeignClients
@EnableDiscoveryClient
//@LoadBalancerClients({
//    @LoadBalancerClient("echoServer")
//})
public class FeignConfig {

    @Value("${echo.url}")
    String echoUrl;

    @Value("feignStore")
    String sslBundle;

    public FeignConfig() {
        log.info("FeignConfig -------------");
    }

    @Bean
    public SSLContext createSSLContext(SslBundles sslBundles) {
        // add this bean, the load balancer won't work
        log.info("Feign client create -------------");
        val bundle = sslBundles.getBundle(sslBundle);
        val ctx = bundle.createSslContext();
        return ctx;
    }

    @Bean
    @ConditionalOnBean(value = SSLContext.class)
    public HttpClient fileServiceFeignClient(SSLContext sslContext) {
        System.out.println("*-*-*-*-*-*-*-*-*-*- append SSL Context to http client 5");

        Registry<ConnectionSocketFactory> socketRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(URIScheme.HTTPS.getId(), new SSLConnectionSocketFactory(sslContext))
                .register(URIScheme.HTTP.getId(), new SSLConnectionSocketFactory(sslContext)/*new PlainConnectionSocketFactory()*/)
                .build();
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(new PoolingHttpClientConnectionManager(socketRegistry))
                .setConnectionManagerShared(true)
                .build();
        return httpClient;
//        return new ApacheHttp5Client(httpClient);
    }

}
