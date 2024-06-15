package org.xyp.sample.spring.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.xyp.sample.spring.db.DbConfig;

@ComponentScan(basePackageClasses = {
    DbConfig.class,
    WebApplication.class,
})
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
