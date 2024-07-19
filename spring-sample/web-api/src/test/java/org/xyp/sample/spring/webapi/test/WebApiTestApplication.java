package org.xyp.sample.spring.webapi.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.xyp.sample.spring.webapi.WebApiApplication;

import static org.springframework.core.env.AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME;

@Import(WebApiApplication.class)
@SpringBootApplication
public class WebApiTestApplication {

    public static final String SCHEMA_SUFFIX = "-pg";

    public static void main(String[] args) {
//        org.hibernate.community.dialect.PostgreSQLLegacyDialect
//        System.setProperty(DEFAULT_PROFILES_PROPERTY_NAME, "test");
        SpringApplication.run(WebApiTestApplication.class, args);
    }
}
