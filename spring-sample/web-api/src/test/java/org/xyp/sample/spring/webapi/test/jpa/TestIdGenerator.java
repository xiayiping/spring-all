package org.xyp.sample.spring.webapi.test.jpa;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.internal.util.StringHelper;
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
import org.xyp.shared.db.datasource.DataSourcePropertiesGroup;
import org.xyp.shared.function.wrapper.ResultOrError;
import org.xyp.shared.db.id.generator.IdGenerator;
import org.xyp.shared.db.id.generator.JdbcConnectionAccessorFactory;
import org.xyp.shared.db.id.generator.table.config.TableIdGeneratorConfig;
import org.xyp.shared.db.id.generator.table.model.BatchIdResult;
import org.xyp.shared.db.id.generator.table.impl.LongIdDbTableGenerator;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
//@ActiveProfiles("postgres")
@ActiveProfiles("test")
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    config = @SqlConfig(
        errorMode = SqlConfig.ErrorMode.FAIL_ON_ERROR//CONTINUE_ON_ERROR
    ),
    scripts = {
        "classpath:/sql/schema-h2.sql",
    })
@Transactional
@Commit
@Rollback(false)
class TestIdGenerator {

    @Autowired
    DataSource dataSource;

    @Autowired
    DataSourcePropertiesGroup idGenPropertiesGroup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void test1() {
        Assertions.assertThat(dataSource).isNotNull();
        Assertions.assertThat(idGenPropertiesGroup).isNotNull();
    }

    @Test
    void test2() throws ExecutionException, InterruptedException, SQLException {
        val fetchDbPeekers = List.<BiConsumer<String, BatchIdResult>>of((e, b) -> ResultOrError.doRun(() -> {
            System.out.println(Thread.currentThread().getName() + " fetch db for " + e);
            Thread.sleep(333);
        }).getResult());
        val updateDbPeekers = List.<BiConsumer<String, BatchIdResult>>of((e, b) -> ResultOrError.doRun(() -> {
            System.out.println(Thread.currentThread().getName() + " updated db for " + e);
            Thread.sleep(333);
        }).getResult());
        val initedDbPeekers = List.<BiConsumer<String, BatchIdResult>>of((e, b) -> ResultOrError.doRun(() -> {
            System.out.println(Thread.currentThread().getName() + " id record initialized for " + e);
            Thread.sleep(333);
        }).getResult());

        final IdGenerator<Long> gen1 = TableIdGeneratorConfig.getLongIdGenerator("main");
        final IdGenerator<Long> gen2 = TableIdGeneratorConfig.getLongIdGenerator("second");
        final IdGenerator<Long> gen3 = TableIdGeneratorConfig.getLongIdGenerator("third");
        if (gen1 instanceof LongIdDbTableGenerator longIdDbTableGenerator) {
            longIdDbTableGenerator.setRecordFetchedPeeks(fetchDbPeekers);
            longIdDbTableGenerator.setRecordUpdatedPeeks(updateDbPeekers);
            longIdDbTableGenerator.setRecordInitializedPeeks(initedDbPeekers);
        }
        if (gen2 instanceof LongIdDbTableGenerator longIdDbTableGenerator) {
            longIdDbTableGenerator.setRecordFetchedPeeks(fetchDbPeekers);
            longIdDbTableGenerator.setRecordUpdatedPeeks(updateDbPeekers);
            longIdDbTableGenerator.setRecordInitializedPeeks(initedDbPeekers);
        }
        if (gen3 instanceof LongIdDbTableGenerator longIdDbTableGenerator) {
            longIdDbTableGenerator.setRecordFetchedPeeks(fetchDbPeekers);
            longIdDbTableGenerator.setRecordUpdatedPeeks(updateDbPeekers);
            longIdDbTableGenerator.setRecordInitializedPeeks(initedDbPeekers);
        }

        val id1Name = "id1";
        val factroy = new JdbcConnectionAccessorFactory() {
            @Override
            public Connection open() throws SQLException {
                return dataSource.getConnection();
            }
        };

        val testTimesTotal = 5;
        val fetchTimes = 30;
        val fetchSize = 7;
        for (int k = 0; k < testTimesTotal; k++) {
            System.out.println("test time: " + k);

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

    @Test
    void test03() {
        val valueColumnName = "value_col";
        val formattedPhysicalTableName = "table";
        val segmentColumnName = "segment";
        final String alias = "tbl";
        final String query = "select " + StringHelper.qualify(alias, valueColumnName)
            + " from " + formattedPhysicalTableName + ' ' + alias
            + " where " + StringHelper.qualify(alias, segmentColumnName) + "=?";
        final LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);

        final Map<String, String[]> updateTargetColumnsMap = singletonMap(alias, new String[]{valueColumnName});
        val s = new PostgreSQLDialect().applyLocksToSql(query, lockOptions, updateTargetColumnsMap);
        System.out.println(s);
    }
}
