package org.xyp.demo.call;

import feign.Client;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;

@Slf4j
public class FeignConfig {

    @Value("${echo.url}")
    String echoUrl;

    @Value("${server.ssl.bundle}")
    String sslBundle;

    public FeignConfig() {
        log.info("-------------");
    }

    @Bean
    public Client feignClient(SslBundles sslBundles) {
        val sslContext = sslBundles.getBundle(sslBundle).createSslContext();
        return new Client.Default(sslContext.getSocketFactory(), new NoopHostnameVerifier());
    }
}
