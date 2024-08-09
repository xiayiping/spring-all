package org.xyp.sample.spring.db;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xyp.id.IdGenerator;
import org.xyp.id.dialect.IdGenDialect;
import org.xyp.id.dialect.IdGenDialectPostgre;
import org.xyp.id.impl.LongIdDbTableGenerator;
import org.xyp.sample.spring.db.id.IdGenPropertiesImpl;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackageClasses = TaskDaoJpa.class)
@ConfigurationPropertiesScan(basePackageClasses = {IdGenPropertiesImpl.class})
public class JpaDbConfig {

    public static volatile org.xyp.id.IdGenerator<Long> SPRING_BEAN = null;

    public JpaDbConfig() {
        log.info("JpaDbConfig ... ...");
    }

    @Bean
    IdGenDialect idGenDialect(IdGenPropertiesImpl idGenProperties) {
        return new IdGenDialectPostgre(idGenProperties);
    }

    @Bean
    IdGenerator<Long> idGenerator(IdGenPropertiesImpl idGenProperties) {
        val generator = new LongIdDbTableGenerator(idGenDialect(idGenProperties));
        SPRING_BEAN = generator;
        return generator;
    }
}
