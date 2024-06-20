package org.xyp.sample.spring.webapi.test.jpa;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xyp.sample.spring.webapi.domain.entity.jpa.Task;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class TestJpaRepo {
    @Autowired
    TaskDaoJpa taskDaoJpa;

    @Test
    void testTask() {
        val taskOpt = taskDaoJpa.findById(Task.TaskId.of(103L));
        System.out.println(taskOpt);
    }
}
