package org.xyp.demo.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoJwtApp {
    private static final String CONFIG_KEY_PROFILE = "spring.profiles.active";

    public static void main(String[] args) {

        SpringApplication.run(DemoJwtApp.class, args);
    }
}