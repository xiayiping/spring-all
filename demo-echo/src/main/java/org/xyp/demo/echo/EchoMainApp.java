package org.xyp.demo.echo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@EnableDiscoveryClient
//@EnableWebSecurity
public class EchoMainApp {
    int a = 99;
    public EchoMainApp() {
        a = 100;
    }

    public static void main(String[] args) {
        SpringApplication.run(EchoMainApp.class, args);
    }

    @Bean
    ApplicationRunner runner () {
        return (args) -> {
//            System.out.println(vaultSecretProperty);
        };
    }
}
