package org.xyp.sample.spring.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.xyp.sample.spring.db.JdbcDbConfig;
import org.xyp.sample.spring.tracing.TracingConfig;

import static org.springframework.core.env.AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME;

@ComponentScan(basePackageClasses = {
    JdbcDbConfig.class,
    TracingConfig.class,
    WebApiApplication.class,
})
@SpringBootApplication
public class WebApiApplication {

    public static void main(String[] args) {
        System.setProperty(DEFAULT_PROFILES_PROPERTY_NAME, "local");
        SpringApplication.run(WebApiApplication.class, args);
    }

}
