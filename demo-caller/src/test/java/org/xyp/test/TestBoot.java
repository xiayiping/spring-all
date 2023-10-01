package org.xyp.test;


import org.junit.jupiter.api.Assertions;
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
        Assertions.assertNotNull(controller);
    }

    @Test
    void echo() {
        String echo = controller.echo();
        System.out.println(echo);
    }
}
