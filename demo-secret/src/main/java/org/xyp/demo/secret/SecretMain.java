package org.xyp.demo.secret;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@AllArgsConstructor
@EnableConfigurationProperties(value = {VaultSecretProperty.class, AwsSecretProperty.class})
public class SecretMain {

    final VaultSecretProperty vaultSecretProperty;

    public static void main(String[] args) {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        log.info("Hello and welcome!");

        // Press Shift+F10 or click the green arrow button in the gutter to run the code.
        for (int i = 1; i <= 5; i++) {

            // Press Shift+F9 to start debugging your code. We have set one breakpoint
            // for you, but you can always add more by pressing Ctrl+F8.
            log.info("i = {}", i);
        }
        SpringApplication.run(SecretMain.class, args);

    }

    @Bean
    public ApplicationRunner runner(VaultSecretProperty vaultSecretProperty) {
        return args -> log.info("{}", vaultSecretProperty);
    }
}