package org.xyp.sample.spring.webapi.test.jdbc;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRule;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRuleDesc;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;
import org.xyp.sample.spring.webapi.repository.jdbc.BatchDaoJdbc;
import org.xyp.sample.spring.webapi.repository.jdbc.TaskDaoJdbc;

import java.util.List;
import java.util.Set;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    config = @SqlConfig(
        errorMode = SqlConfig.ErrorMode.CONTINUE_ON_ERROR
    ),
    scripts = {
        "classpath:/sql/schema.sql",
        "classpath:/sql/schema-mssql.sql",
    })
@SpringBootTest
@ActiveProfiles("test")
class TestJdbcRepo {

    @Autowired
    TaskDaoJdbc taskDaoJdbc;

    @Autowired
    BatchDaoJdbc batchDaoJdbc;

    Task.TaskId id;
    Batch.BatchId bid;

    @Test
    @DisplayName("save batch")
    @Order(0)
    void testSaveBatch() {
        val batch = Batch.builder()
            .companyId(22)
            .batchName("bn")
            .batchRules(List.of(
                new BatchRule(null, "rule1", Set.of(
                    new BatchRuleDesc("r1"),
                    new BatchRuleDesc("r2")
                )),
                new BatchRule(null, "rule2", Set.of(
                    new BatchRuleDesc("r3"),
                    new BatchRuleDesc("r4")
                ))
            ))
            .build();
        try {
            batchDaoJdbc.save(batch);
            Assertions.assertThat(batch.getId()).isNotNull();
            Assertions.assertThat(batch.getBatchRules()).isNotEmpty();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        bid = batch.getId();
    }

    @Test
    @DisplayName("save batch again")
    @Order(1)
    void saveBatchAgain() {
        val bt = batchDaoJdbc.findById(bid);
        Assertions.assertThat(bt).isNotEmpty();
        Assertions.assertThat(bt.get().getBatchRules()).isNotEmpty();
        System.out.println(bt.get());
        val btnew = bt.get();
        btnew.setBatchName("new name 2");
        val savedBt = batchDaoJdbc.save(btnew);
        Assertions.assertThat(savedBt.getBatchRules()).isNotEmpty();
        System.out.println(savedBt);
    }

    @Test
    @DisplayName("save task")
    @Order(2)
    void testSave() {
        val task = Task.of(1, "test",
            Batch.BatchRef.<Batch>of(
                Batch.builder()
                    .id(bid)
                    .build()
            )
        );
        taskDaoJdbc.save(task);
        Assertions.assertThat(task).isNotNull();
        id = task.getId();
    }

    //    @Disabled("no needed")
    @Test
    @DisplayName("fetch task")
    @Order(3)
    void test() {
        val task = taskDaoJdbc.findById(id);
        System.out.println(task);
        Assertions.assertThat(task).isNotEmpty();
        Assertions.assertThat(task.get().getBatch()).isNotNull();
    }

}
