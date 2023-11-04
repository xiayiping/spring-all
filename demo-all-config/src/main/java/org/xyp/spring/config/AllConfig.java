package org.xyp.spring.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AllConfigProperties.class})
public class AllConfig {
}
