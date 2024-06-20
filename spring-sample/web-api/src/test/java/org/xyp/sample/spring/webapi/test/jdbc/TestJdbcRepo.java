package org.xyp.sample.spring.webapi.test.jdbc;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.xyp.sample.plain.share.All;
import org.xyp.sample.spring.webapi.domain.entity.jdbc.Batch;
import org.xyp.sample.spring.webapi.domain.entity.jdbc.Task;
import org.xyp.sample.spring.webapi.repository.jdbc.BatchDaoJdbc;
import org.xyp.sample.spring.webapi.repository.jdbc.TaskDaoJdbc;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class TestJdbcRepo {

    @Autowired
    TaskDaoJdbc taskDaoJdbc;

    @Autowired
    BatchDaoJdbc batchDaoJdbc;

    @DisplayName("fetch from other project")
    @Test
    void testFetchFromPlainProj() {
        Assertions.assertThat(All.ALL).isEqualTo(110);
    }

    @Disabled("no needed")
    @Test
    @DisplayName("fetch task")
    void test() {
        val task = taskDaoJdbc.findById(103L);
        System.out.println(task);
        Assertions.assertThat(task).isNotNull();
    }

    @Disabled("no needed")
    @Test
    @DisplayName("fetch task")
    void testBatch() {
        val batch = batchDaoJdbc.findById(6L);
        System.out.println(batch);
        Assertions.assertThat(batch).isNotNull();

        val tasksByBatch = taskDaoJdbc.findTasksByBatch(AggregateReference.to(6L));
        System.out.println(tasksByBatch);
    }

    @Disabled("no needed")
    @Test
    @DisplayName("save task")
    void testSave() {
        val task = Task.of(1, "test", Batch.BatchId.of(1));
        taskDaoJdbc.save(task);
        Assertions.assertThat(task).isNotNull();
    }
}
