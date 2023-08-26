package org.xyp.demo.call;

import feign.Client;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@EnableFeignClients
//@ImportAutoConfiguration(FeignAutoConfiguration.class)
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

    // add this the load balancer won't work
//    @Bean
//    public Client feignClient(SslBundles sslBundles) {
//        log.info("Feign client create -------------");
//        val sslContext = sslBundles.getBundle(sslBundle).createSslContext();
//        return new Client.Default(sslContext.getSocketFactory(), new NoopHostnameVerifier());
//    }
}
