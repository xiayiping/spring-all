package org.xyp.sample.spring.secret.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.xyp.sample.spring.secret.config.SecretConfig;


@ComponentScan(basePackageClasses = {
    SecretConfig.class,
})
@SpringBootApplication
public class SecretTestRootApplication {
}
