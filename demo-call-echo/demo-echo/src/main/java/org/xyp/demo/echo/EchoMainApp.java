package org.xyp.demo.echo;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

    // A TimedAspect is needed for @Timed annotation
    @Bean
    public TimedAspect createTimedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }
}
