package org.xyp.demo.echo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.xyp.demo.api.secret.config.VaultKeyStoreSecretProperty;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableDiscoveryClient
//@EnableWebSecurity
@EnableConfigurationProperties(VaultKeyStoreSecretProperty.class)
public class EchoMainApp {
    int a = 99;
    public EchoMainApp() {
        a = 100;
    }

    public static void main(String[] args) {
        SpringApplication.run(EchoMainApp.class, args);
    }

    @Autowired
    VaultKeyStoreSecretProperty vaultSecretProperty;
    @Bean
    ApplicationRunner runner () {
        return (args) -> {
            System.out.println(vaultSecretProperty);
        };
    }
}
