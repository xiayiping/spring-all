package org.xyp.sample.spring.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public SecurityConfig() {
        log.info("using SecurityConfig ......");
    }
}
