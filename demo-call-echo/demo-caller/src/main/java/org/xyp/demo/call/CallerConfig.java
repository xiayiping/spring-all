package org.xyp.demo.call;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class CallerConfig {

    @ConditionalOnProperty(
        name = "all-configs.server-ssl.demo-echo.enabled",
        havingValue = "false",
        matchIfMissing = true)
    public static class NormalConfig {
        @Bean("normal")
        @Primary
        public RestTemplate normalRestTemplate(
            RestTemplateBuilder builder,
            @Autowired
            @Value("${all-configs.server-port.demo-echo}") String echoPort) {
            log.info("---- creating normal restTemplate");
            return builder
                .rootUri("http://localhost:" + echoPort)
                .build();
        }
    }

    @ConditionalOnProperty(
        name = "all-configs.server-ssl.demo-echo.enabled",
        havingValue = "true",
        matchIfMissing = false)
    public static class HttpsConfig {
        @Bean("https")
        public RestTemplate httpsRestTemplate(
            RestTemplateBuilder builder,
            SslBundles bundles,
            @Autowired
            @Value("${all-configs.server-port.demo-echo}") String echoPort
        ) {
            log.info("---- creating https restTemplate");
            return builder
                .rootUri("https://localhost:" + echoPort)
                .setSslBundle(bundles.getBundle("defaultBundle"))
                .build();
        }
    }

    @Bean
    public TimedAspect createTimedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    @Bean
    public MeterFilter commonMeterFilter() {
        return MeterFilter.commonTags(
            Tags.of("dynamic_tag", "cc123")
        );
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("region_xyp", "shanghai");
    }


}
