package org.xyp.sample.spring.oauth2client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import static org.springframework.core.env.AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME;

@SpringBootApplication
public class Oauth2ClientApplication {

    public static void main(String[] args) {
        System.setProperty(DEFAULT_PROFILES_PROPERTY_NAME, "local");
        SpringApplication.run(Oauth2ClientApplication.class, args);
    }

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(rs -> rs
                .path("/hello", "/api/**")
                .filters(GatewayFilterSpec::tokenRelay)
                .uri("http://127.0.0.1:8082")
            )
            .build();
    }
}
