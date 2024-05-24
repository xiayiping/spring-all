package org.xyp.spring.caller.log4j2.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;
import org.xyp.spiring.caller.log4j2.UseLog4j2Application;

@SpringBootApplication
@ImportAutoConfiguration(UseLog4j2Application.class)
@ActiveProfiles("test")
public class Log4j2TestRootApplication {
    public static void main(String[] args) {
        SpringApplication.run(Log4j2TestRootApplication.class);
    }
}
