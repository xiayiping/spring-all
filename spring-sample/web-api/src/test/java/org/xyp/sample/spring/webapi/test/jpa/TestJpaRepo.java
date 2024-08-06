package org.xyp.sample.spring.webapi.test.jpa;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;
import org.xyp.function.Fun;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRule;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRuleDesc;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;
import org.xyp.sample.spring.webapi.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.repository.jpa.BatchRecordDaoJpa;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;
import org.xyp.sample.spring.webapi.repository.mybatis.BatchDaoMybatis;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    config = @SqlConfig(
        errorMode = SqlConfig.ErrorMode.FAIL_ON_ERROR//CONTINUE_ON_ERROR
    ),
    scripts = {
//        "classpath:/sql/schema.sql",
//        "classpath:/sql/schema-mssql.sql",
        "classpath:/sql/schema-h2.sql",
    })
@Transactional
@Commit
@Rollback(false)
class TestJpaRepo {
    @Autowired
    TaskDaoJpa taskDaoJpa;
    @Autowired
    BatchDaoJpa batchDaoJpa;
    @Autowired
    BatchRecordDaoJpa batchRecordDaoJpa;
    @Autowired
    BatchDaoMybatis batchDaoMybatis;

    Batch.BatchId id = null;
    Batch bt = null;

    @Test
    @Order(0)
    @DisplayName("save first batch")
    void testSaveBatchFirst() {
        val batch = new Batch(2, "batch");
        batch.setBatchRules(List.of(
            new BatchRule(null, "rule1", Set.of(
                new BatchRuleDesc("r1"),
                new BatchRuleDesc("r2")
            )),
            new BatchRule(null, "rule2", Set.of(
                new BatchRuleDesc("r3"),
                new BatchRuleDesc("r3")
            ))
        ));
        val savedBatch = batchDaoJpa.save(batch);
        Assertions.assertThat(savedBatch.getId()).isNotNull();
        id = savedBatch.getId();
        bt = savedBatch;
    }

    @Test
    @Order(2)
    @DisplayName("save batch again")
    void testSaveBatchAgain() {
        val batch = bt;
        batch.setBatchName("jpa new batck");
        batch.setBatchRules(
            batch.getBatchRules().stream()
                .map(Fun.consumeSelf(rr -> rr.setRuleName(rr.getRuleName() + " dd")))
                .toList()
        );
        val savedBatch = batchDaoJpa.save(batch);
        Assertions.assertThat(savedBatch.getId()).isNotNull();
        id = savedBatch.getId();
    }

    @Test
    @Order(5)
    @DisplayName("fetch batch record mybatis")
    void testSFetchRecordMybatis() {
        log.info("find by id {}", id);
        val batchRecord = batchDaoMybatis.findById(id);
        System.out.println(batchRecord);
        Assertions.assertThat(batchRecord.batchRules()).isNotEmpty();
        System.out.println(batchRecord.batchRules());
    }

    @Test
    @Order(10)
    @DisplayName("test save many tasks")
    void testSaveTask() {
        val btch = batchDaoJpa.findById(id).get();
        val tasks1 = IntStream.range(0, 97)
            .mapToObj(i -> Task.builder().companyId(i).employeeId("em")
                .batch(Batch.BatchRef.of(btch))
                .build())
            .toList();
        taskDaoJpa.saveAll(tasks1);

        val tasks2 = IntStream.range(0, 98)
            .mapToObj(i -> Task.builder().companyId(i).employeeId("em")
                .batch(Batch.BatchRef.of(btch))
                .build())
            .toList();
        val last = taskDaoJpa.saveAll(tasks2).getLast();
        Assertions.assertThat(last).isNotNull();
        Assertions.assertThat(last.getId()).isNotNull();
    }

    @Test
    @Order(20)
    @DisplayName("test save and fetch batch")
    void testSaveBatch() {
        val batch = new Batch(2, "batch");
        val savedBatch = batchDaoJpa.save(batch);
        val task = Task.builder().companyId(1).employeeId("em")
            .batch(Batch.BatchRef.of(savedBatch))
            .build();
        val savedTask = taskDaoJpa.save(task);
        Assertions.assertThat(savedTask.getId()).isNotNull();

        val fetchedBatch = batchDaoJpa.findById(savedBatch.getId());
        Assertions.assertThat(fetchedBatch).isNotEmpty();
        id = fetchedBatch.get().getId();
    }

    @Test
    @Order(30)
    @DisplayName("test fetch last saved batch")
    void testSaveBatch2() {
        val fetchedBatch = batchDaoJpa.findWithRulesById(id);
        Assertions.assertThat(fetchedBatch).isNotEmpty();
    }

    @Test
    @Order(40)
    @DisplayName("batch as aggregate root")
    void testSaveBatch3() {
        val batch = new Batch(2, "batch");
        val savedBatch = batchDaoJpa.save(batch);
        id = savedBatch.getId();
        log.info("saved id {}", id);
    }

    @Test
    @Order(50)
    @DisplayName("batch as aggregate root fetch")
    void testSaveBatch4() {
        log.info("find by id {}", id);
        val fetchedBatch = batchDaoJpa.findWithRulesById(id);
        log.info("find by id result {}", fetchedBatch);
        Assertions.assertThat(fetchedBatch).isNotEmpty();
        System.out.println(fetchedBatch.get());
    }

    @Disabled("hibernate doesn't support record (hibernate requires non-argument constructor)")
    @Test
    @Order(60)
    @DisplayName("fetch batch record")
    void testSFetchRecord() {
        log.info("find by id {}", id);
        Assertions.assertThatThrownBy(() -> {
            batchRecordDaoJpa.findWithRulesById(id);
        }).isInstanceOf(RuntimeException.class);
    }
}
