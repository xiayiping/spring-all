package org.xyp.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xyp.demo.call.CallerController;
import org.xyp.demo.call.CallerMainApp;

@SpringBootTest(classes = CallerMainApp.class)
class TestBoot {

    @Autowired
    CallerController controller;

    @Test
    void abc() {
        Assertions.assertThat(controller).isNotNull();
    }

    @Test
    void echo() {
        String echo = controller.echo();
        org.assertj.core.api.Assertions.assertThat(echo).isNotNull();
    }
}
