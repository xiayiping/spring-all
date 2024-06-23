package org.xyp.sample.spring.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xyp.sample.spring.webapi.repository.jdbc.TaskDaoJdbc;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;

import java.util.List;

@Slf4j
//@AllArgsConstructor
@Configuration(
    proxyBeanMethods = false
)
@EnableJdbcRepositories(basePackageClasses = TaskDaoJdbc.class)
@EnableJpaRepositories(basePackageClasses = TaskDaoJpa.class)
public class JdbcDbConfig extends AbstractJdbcConfiguration {
    List<?> converters = null;

    public JdbcDbConfig(List<Converter<?, ?>> converters) {
        this.converters = converters;
    }

    @Override
    protected List<?> userConverters() {

        log.info("create id converters {}", converters.stream().map(c -> c.getClass().getName()).toList());
        return converters;
    }
}
