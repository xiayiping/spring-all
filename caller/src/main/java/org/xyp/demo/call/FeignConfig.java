package org.xyp.demo.call;

import feign.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;

@Slf4j
public class FeignConfig {

    @Value("${server.ssl.bundle}")
    String sslBundleKey;

    public FeignConfig() {
        log.info("init customized feign config");
    }

    @Bean
    public Client client(SslBundles sslBundles) {
        log.info("init customized feign client");
        return new Client.Default(
            sslBundles.getBundle(sslBundleKey).createSslContext().getSocketFactory(),
            new NoopHostnameVerifier());
    }
}
