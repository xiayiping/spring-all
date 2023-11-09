package org.xyp.demo.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConfigSecretMain {

    @Value("${sql_password:222}")
    String pwd;

    @Value("${private_key:pp222}")
    String pwd2;

    @Value("${key_store:pp333}")
    String pwd3;

    @Value("${crypto.symmetric-secrets.123.secret:ref}")
    String pwd4;

    public static void main(String[] args) {
        SpringApplication.run(ConfigSecretMain.class);
    }

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            System.out.println(pwd);
            System.out.println(pwd2);
            System.out.println(pwd3);
            System.out.println(pwd4);
        };
    }
}
