package org.xyp.sample.spring.webapi.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.xyp.sample.spring.webapijwt.WebApiJwtApplication;

import static org.springframework.core.env.AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME;

@Import(WebApiJwtApplication.class)
@SpringBootApplication
public class WebApiTestApplication {
    public static void main(String[] args) {
        System.setProperty(DEFAULT_PROFILES_PROPERTY_NAME, "test");
        SpringApplication.run(WebApiTestApplication.class, args);
    }
}
