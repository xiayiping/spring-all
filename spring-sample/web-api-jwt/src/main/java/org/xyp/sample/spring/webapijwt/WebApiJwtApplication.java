package org.xyp.sample.spring.webapijwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApiJwtApplication {

    public static final String CONTEXT = "/api/jwt";

    public static void main(String[] args) {
        SpringApplication.run(WebApiJwtApplication.class, args);
    }

}
