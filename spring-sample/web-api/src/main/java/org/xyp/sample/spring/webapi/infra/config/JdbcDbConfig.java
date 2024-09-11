package org.xyp.sample.spring.webapi.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.xyp.sample.spring.webapi.domain.task.repository.jdbc.TaskDaoJdbc;

import java.util.List;

@Slf4j
//@AllArgsConstructor
@Configuration(
    proxyBeanMethods = false
)
@EnableJdbcRepositories(basePackageClasses = TaskDaoJdbc.class)
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
