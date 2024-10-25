package org.xyp.sample.spring.webapi.test.jpa;


import com.jayway.jsonpath.internal.Utils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;
import org.xyp.id.IdGenerator;
import org.xyp.id.JdbcConnectionAccessorFactory;
import org.xyp.id.dialect.IdGenDialectPostgre;
import org.xyp.id.impl.LongIdDbTableGenerator;
import org.xyp.sample.spring.db.id.IdGenPropertiesImpl;
import org.xyp.sample.spring.webapi.infra.config.JpaDbConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
//@ActiveProfiles("test")
@ActiveProfiles("mssql")
//@Sql(
//    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
//    config = @SqlConfig(
//        errorMode = SqlConfig.ErrorMode.FAIL_ON_ERROR//CONTINUE_ON_ERROR
//    ),
//    scripts = {
//        "classpath:/sql/schema.sql",
//        "classpath:/sql/schema-mssql.sql",
//        "classpath:/sql/schema-h2.sql",
//    })
@Transactional
@Commit
@Rollback(false)
class TestIdGenerator {

    @Autowired
    DataSource dataSource;

    @Autowired
    IdGenPropertiesImpl idGenProperties;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void test1() {
        Assertions.assertThat(dataSource).isNotNull();
        Assertions.assertThat(idGenProperties).isNotNull();
    }

    @Test
    void test2() throws ExecutionException, InterruptedException, SQLException {
        val config = new JpaDbConfig();
        final IdGenerator<Long> gen1 = new LongIdDbTableGenerator(config.idGenDialect(idGenProperties));
        final IdGenerator<Long> gen2 = new LongIdDbTableGenerator(config.idGenDialect(idGenProperties));
        final IdGenerator<Long> gen3 = new LongIdDbTableGenerator(config.idGenDialect(idGenProperties));

        val id1Name = "id1";
        val factroy = new JdbcConnectionAccessorFactory() {
            @Override
            public Connection open() throws SQLException {
                return dataSource.getConnection();
            }
        };

        val fetchTimes = 3;
        for (int k = 0; k < 66; k++) {
            System.out.println("test time: " + k);
//            val defaultFetchSize = 15 + k;
//            val stepSize = new Random().nextInt(5) + 1;
//            val fetchSize = new Random().nextInt(30) + 1;

            val fetchSize = 3;

            val t = k;
            ConcurrentHashMap<Long, Long> ids = new ConcurrentHashMap<>();
            val task1 = ForkJoinPool.commonPool().submit(() -> {
                Set<Long> subIds = new HashSet<>();
                for (int i = 0; i < fetchTimes; i++) {
                    val gened = gen1.nextId(id1Name, fetchSize, factroy);
                    System.out.println(Thread.currentThread().getName() + gened + " " + subIds.size() + " " + t);
                    subIds.addAll(gened);
                }
                Assertions.assertThat(subIds).hasSize(fetchTimes * fetchSize);
                return subIds;
            });
            val task2 = ForkJoinPool.commonPool().submit(() -> {
                Set<Long> subIds = new HashSet<>();
                for (int i = 0; i < fetchTimes; i++) {
                    val gened = gen2.nextId(id1Name, fetchSize, factroy);
                    System.out.println(Thread.currentThread().getName() + gened + " " + subIds.size() + " " + t);
                    subIds.addAll(gened);
                }
                Assertions.assertThat(subIds).hasSize(fetchTimes * fetchSize);
                return subIds;
            });
            val task3 = ForkJoinPool.commonPool().submit(() -> {
                Set<Long> subIds = new HashSet<>();
                for (int i = 0; i < fetchTimes; i++) {
                    val gened = gen3.nextId(id1Name, fetchSize, factroy);
                    System.out.println(Thread.currentThread().getName() + gened + " " + subIds.size() + " " + t);
                    subIds.addAll(gened);
                }
                Assertions.assertThat(subIds).hasSize(fetchTimes * fetchSize);
                return subIds;
            });
            task1.join();
            task2.join();
            task3.join();

            ids.putAll(((Set<Long>) (task1.get())).stream().collect(Collectors.toMap(i -> i, i -> i)));
            ids.putAll(((Set<Long>) (task2.get())).stream().collect(Collectors.toMap(i -> i, i -> i)));
            ids.putAll(((Set<Long>) (task3.get())).stream().collect(Collectors.toMap(i -> i, i -> i)));
            Assertions.assertThat(ids).hasSize(3 * fetchTimes * fetchSize);
        }


    }
}
