package org.xyp.demo.jwt;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoJwtApp {
    private static final String CONFIG_KEY_PROFILE = "spring.profiles.active";

    public static void main(String[] args) {
        val profile = System.getProperty(CONFIG_KEY_PROFILE);
        if (StringUtils.isBlank(profile)) {
            System.setProperty(CONFIG_KEY_PROFILE, "dev");

        }

        SpringApplication.run(DemoJwtApp.class, args);
    }
}