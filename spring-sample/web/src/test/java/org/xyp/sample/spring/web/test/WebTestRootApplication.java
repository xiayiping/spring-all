package org.xyp.sample.spring.web.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.xyp.sample.spring.web.WebApplication;

@Import(WebApplication.class)
@SpringBootApplication
public class WebTestRootApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebTestRootApplication.class, args);
    }
}
