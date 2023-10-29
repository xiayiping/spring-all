package org.xyp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@Order(-10)
public class BootstrapConfig {

    public BootstrapConfig() {
        log.info("construct --------------- bootstrap ");}

    @Bean
    public Object someObj() {
        log.info("create --------------- bootstrap obj ");
        return new Object();
    }
}
