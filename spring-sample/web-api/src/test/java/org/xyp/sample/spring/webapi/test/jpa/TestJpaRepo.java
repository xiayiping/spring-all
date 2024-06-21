package org.xyp.sample.spring.webapi.test.jpa;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.xyp.sample.spring.webapi.domain.entity.jpa.Task;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
//@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
//    statements = {
//        "use test"
//    })
@Transactional
@Commit
@Rollback(false)
class TestJpaRepo {
    @Autowired
    TaskDaoJpa taskDaoJpa;

    @Test
    void testTask() {
        val taskOpt = taskDaoJpa.findById(Task.TaskId.of(103L));
        System.out.println(taskOpt);

    }

    @Test
    void testSaveTask() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 977; i++) {
            val task = Task.builder().companyId(1).employeeId("em").build();
            taskDaoJpa.save(task);
        }

        Task last = null;
        for (int i = 0; i < 988; i++) {
            val task = Task.builder().companyId(1).employeeId("em").build();
            last = taskDaoJpa.save(task);
        }
        val end = System.currentTimeMillis();
        Assertions.assertThat(last).isNotNull();
        Assertions.assertThat(last.getId()).isNotNull();
    }
}
