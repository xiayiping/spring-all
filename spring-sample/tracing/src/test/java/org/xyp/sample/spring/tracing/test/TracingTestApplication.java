package org.xyp.sample.spring.tracing.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.xyp.sample.spring.tracing.TracingConfig;

@ComponentScan(basePackageClasses = {
    TracingConfig.class,
})
@SpringBootApplication
public class TracingTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TracingTestApplication.class, args);
    }
}
