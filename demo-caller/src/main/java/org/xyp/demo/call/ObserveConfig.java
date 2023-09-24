package org.xyp.demo.call;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.ObservationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
//@Configuration(proxyBeanMethods = false)
public class ObserveConfig {

    public ObserveConfig() {
        log.info("starting init auto observation config ");
    }

//    @Bean
    ObservationFilter orgFilter(@Value("${spring.application.org}") String org) {
        log.info("create ObservationFilter with org {} ", org);
        return context -> {
            log.info("add low cardinality org {}", org);
            context.addHighCardinalityKeyValue(KeyValue.of("org", org));
            return context;
        };
    }
}
