package org.xyp.demo.call;

import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
//@EnableFeignClients
@EnableDiscoveryClient
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

    @Bean("sslHttpClient")
    @ConditionalOnBean(value = SSLContext.class)
    public CloseableHttpClient fileServiceFeignClient(SSLContext sslContext) {
        System.out.println("*-*-*-*-*-*-*-*-*-*- append SSL Context to http client 5");

        Registry<ConnectionSocketFactory> socketRegistry =
            RegistryBuilder.<ConnectionSocketFactory>create()
            .register(URIScheme.HTTPS.getId(), new SSLConnectionSocketFactory(sslContext))
            .register(URIScheme.HTTP.getId(), new PlainConnectionSocketFactory())
            .build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setConnectionManager(new PoolingHttpClientConnectionManager(socketRegistry))
            .setConnectionManagerShared(true)
            .build();
        return httpClient;
    }

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Decoder springDecoder() {
        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
    }

    @Bean
    public ErrorDecoder  uaaErrorDecoder(Decoder decoder) {
        return (methodKey, response) -> {
            try {

                return new RuntimeException(response.body().toString());

            } catch (Exception e) {
                e.printStackTrace();
                return new RuntimeException("");
            }
        };
    }
}
