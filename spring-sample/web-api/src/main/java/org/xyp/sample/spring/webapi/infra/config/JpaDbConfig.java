package org.xyp.sample.spring.webapi.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.xyp.sample.spring.webapi.domain.task.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.domain.task.repository.jpa2.StockRepository;
import org.xyp.sample.spring.webapi.domain.task.repository.mybatis.BatchDaoMybatis;
import org.xyp.shared.db.id.generator.IdGenerator;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackageClasses = {StockRepository.class, BatchDaoJpa.class})
@MapperScan(basePackageClasses = BatchDaoMybatis.class)
//@ConfigurationPropertiesScan(basePackageClasses = {IdGenPropertiesImpl.class})
public class JpaDbConfig {

    public static volatile IdGenerator<Long> SPRING_BEAN = null;

    public JpaDbConfig() {
        log.info("JpaDbConfig ... ...");
    }

//    //    @Bean
//    public IdGenDialect idGenDialect(IdGenProperties idGenProperties) {
//        return switch (idGenProperties.getDialect()) {
//            case MSSQL -> new IdGenDialectMssql(idGenProperties);
//            case H2 -> new IdGenDialectH2(idGenProperties);
//            default -> new IdGenDialectPostgres(idGenProperties);
//        };
//    }
//
//    //    @Bean
//    IdGenerator<Long> idGenerator(IdGenProperties idGenProperties) {
//        val generator = new LongIdDbTableGenerator(idGenDialect(idGenProperties), idGenProperties);
//        SPRING_BEAN = generator;
//        System.out.println(SPRING_BEAN);
//        return generator;
//    }
}
