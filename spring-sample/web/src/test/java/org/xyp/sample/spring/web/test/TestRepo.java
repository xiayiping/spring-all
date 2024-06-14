package org.xyp.sample.spring.web.test;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.xyp.sample.plain.share.All;
import org.xyp.sample.spring.web.WebApplication;
import org.xyp.sample.spring.web.domain.entity.Task;
import org.xyp.sample.spring.web.repository.jdbc.TaskDao;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class TestRepo {

    @Autowired
    TaskDao taskDao;

    @DisplayName("fetch from other project")
    @Test
    void testFetchFromPlainProj() {
        Assertions.assertThat(All.ALL).isEqualTo(110);
    }

    @Disabled("no needed")
    @Test
    @DisplayName("fetch task")
    void test() {
        val task = taskDao.findById(103L);
        System.out.println(task);
        Assertions.assertThat(task).isNotNull();
    }

    @Disabled("no needed")
    @Test
    @DisplayName("save task")
    void testSave() {
        val task = Task.of(1, "test");
        taskDao.save(task);
        Assertions.assertThat(task).isNotNull();
    }
}
