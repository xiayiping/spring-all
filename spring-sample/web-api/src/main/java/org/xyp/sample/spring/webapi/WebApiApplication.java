package org.xyp.sample.spring.webapi;

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.xyp.sample.spring.db.JdbcDbConfig;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;

@ComponentScan(basePackageClasses = {
    JdbcDbConfig.class,
    WebApiApplication.class,
})
@SpringBootApplication
public class WebApiApplication {

    public static void main(String[] args) {
        val builder = Task.builder();
        SpringApplication.run(WebApiApplication.class, args);
    }

}
