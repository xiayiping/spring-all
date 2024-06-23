package org.xyp.sample.spring.webapi.test.jpa;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.xyp.function.Fun;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRule;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRuleDesc;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;
import org.xyp.sample.spring.webapi.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    scripts = {
        "classpath:/sql/schema.sql"
    })
@Transactional
@Commit
@Rollback(false)
class TestJpaRepo {
    @Autowired
    TaskDaoJpa taskDaoJpa;
    @Autowired
    BatchDaoJpa batchDaoJpa;

    @Test
    @DisplayName("test 0")
    void testTask() {
        val taskOpt = taskDaoJpa.findById(Task.TaskId.of(103L));
        System.out.println(taskOpt);

    }

    Batch.BatchId id = null;

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
    }

    @Test
    @Order(2)
    @DisplayName("save batch again")
    void testSaveBatchAgain() {
        val batch = batchDaoJpa.findWithRulesById(id).get();
        batch.setBatchName("jpa new batck");
        batch.setBatchRules(
            batch.getBatchRules().stream()
                .map(r -> Fun.update(r, rr -> rr.setRuleName(rr.getRuleName() + " dd")))
                .toList()
        );
        val savedBatch = batchDaoJpa.save(batch);
        Assertions.assertThat(savedBatch.getId()).isNotNull();
        id = savedBatch.getId();
    }

    @Test
    @Order(10)
    @DisplayName("test save many tasks")
    void testSaveTask() {
        val bt = batchDaoJpa.findById(id).get();
        val tasks1 = IntStream.range(0, 97)
            .mapToObj(i -> Task.builder().companyId(i).employeeId("em")
                .batch(Batch.BatchRef.of(bt))
                .build())
            .toList();
        taskDaoJpa.saveAll(tasks1);

        val tasks2 = IntStream.range(0, 98)
            .mapToObj(i -> Task.builder().companyId(i).employeeId("em")
                .batch(Batch.BatchRef.of(bt))
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
//        val fetchedBatch = batchDaoJpa.findById(id);
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
        RepositoryFactorySupport ss;
        log.info("find by id {}", id);
        val fetchedBatch = batchDaoJpa.findWithRulesById(id);
        log.info("find by id result {}", fetchedBatch);
        Assertions.assertThat(fetchedBatch).isNotEmpty();
        System.out.println(fetchedBatch.get());
    }
}
