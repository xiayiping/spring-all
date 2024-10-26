package org.xyp.sample.spring.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.xyp.sample.spring.webapi.infra.config.JdbcDbConfig;
import org.xyp.sample.spring.tracing.TracingConfig;
import org.xyp.sample.spring.webapi.web.config.WebApiProperties;
import org.xyp.shared.id.generator.table.config.IdGeneratorConfig;

@ComponentScan(basePackageClasses = {
    JdbcDbConfig.class,
    TracingConfig.class,
    IdGeneratorConfig.class,
    WebApiApplication.class,
})
@EnableConfigurationProperties({
    WebApiProperties.class
})
@SpringBootApplication
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }

}
