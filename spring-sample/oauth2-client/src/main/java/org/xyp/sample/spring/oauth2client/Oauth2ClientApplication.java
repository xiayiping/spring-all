package org.xyp.sample.spring.oauth2client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Oauth2ClientApplication {

    public static void main(String[] args) {
        System.out.println("starting gateway 3 ......");
        SpringApplication.run(Oauth2ClientApplication.class, args);
    }

}
