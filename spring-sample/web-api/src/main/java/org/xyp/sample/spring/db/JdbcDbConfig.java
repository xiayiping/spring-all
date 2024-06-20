package org.xyp.sample.spring.db;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.xyp.sample.spring.webapi.domain.entity.jdbc.Task;

import java.util.Arrays;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Configuration(
    proxyBeanMethods = false
)
public class JdbcDbConfig extends AbstractJdbcConfiguration {

    final Task.TaskId.TaskIdReadingConverter taskIdReadingConverter;
    final Task.TaskId.TaskIdWritingConverter taskIdWritingConverter;

    @Override
    protected List<?> userConverters() {
        val idConverters = Arrays.asList(
            taskIdReadingConverter,
            taskIdWritingConverter
        );

        log.info("create id converters {}", idConverters.stream().map(c -> c.getClass().getName()).toList());
        return idConverters;
    }
}
