package org.xyp.sample.spring.webapi.infra.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.mybatis.spring.annotation.MapperScan;
import org.xyp.shared.id.generator.IdGenerator;
import org.xyp.shared.id.generator.table.dialect.*;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xyp.shared.id.generator.table.impl.LongIdDbTableGenerator;
import org.xyp.sample.spring.db.id.IdGenPropertiesImpl;
import org.xyp.sample.spring.webapi.domain.task.repository.jpa.TaskDaoJpa;
import org.xyp.sample.spring.webapi.domain.task.repository.mybatis.BatchDaoMybatis;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackageClasses = TaskDaoJpa.class)
@MapperScan(basePackageClasses = BatchDaoMybatis.class)
@ConfigurationPropertiesScan(basePackageClasses = {IdGenPropertiesImpl.class})
public class JpaDbConfig {

    public static volatile IdGenerator<Long> SPRING_BEAN = null;

    public JpaDbConfig() {
        log.info("JpaDbConfig ... ...");
    }

    @Bean
    public IdGenDialect idGenDialect(IdGenProperties idGenProperties) {
        return switch (idGenProperties.getDialect()) {
            case MSSQL -> new IdGenDialectMssql(idGenProperties);
            case H2 -> new IdGenDialectH2(idGenProperties);
            default -> new IdGenDialectPostgres(idGenProperties);
        };
    }

    @Bean
    IdGenerator<Long> idGenerator(IdGenPropertiesImpl idGenProperties) {
        val generator = new LongIdDbTableGenerator(idGenDialect(idGenProperties), idGenProperties);
        SPRING_BEAN = generator;
        return generator;
    }
}
