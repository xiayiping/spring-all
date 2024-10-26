package org.xyp.shared.id.generator.table.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xyp.shared.id.generator.IdGenerator;
import org.xyp.shared.id.generator.table.hibernate.HibernateIdTableGenerator;
import org.xyp.shared.id.generator.table.dialect.*;
import org.xyp.shared.id.generator.table.impl.LongIdDbTableGenerator;

@Slf4j
@Configuration
@EnableConfigurationProperties({IdGenPropertiesImpl.class})
public class IdGeneratorConfig {

    public IdGeneratorConfig() {
        log.info("id generator config loaded ... ...");
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
    IdGenerator<Long> idGenerator(IdGenProperties idGenProperties) {
        val generator = new LongIdDbTableGenerator(idGenDialect(idGenProperties), idGenProperties);
        HibernateIdTableGenerator.SPRING_BEAN = generator;
        return generator;
    }

}
