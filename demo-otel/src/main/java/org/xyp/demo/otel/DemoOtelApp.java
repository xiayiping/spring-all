package org.xyp.demo.otel;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class DemoOtelApp {
    private static final String CONFIG_KEY_PROFILE = "spring.profiles.active";

    public static void main(String[] args) {
        val profile = System.getProperty(CONFIG_KEY_PROFILE);
        if (StringUtils.isBlank(profile)) {
            System.setProperty(CONFIG_KEY_PROFILE, "dev");
        }
        SpringApplication.run(DemoOtelApp.class, args);
    }

}
