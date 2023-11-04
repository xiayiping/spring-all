package org.xyp.demo.echo.test;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xyp.demo.echo.EchoController;
import org.xyp.demo.echo.EchoMainApp;

@Slf4j
@SpringBootTest(classes = {EchoMainApp.class})
class TestController {

    @Autowired
    EchoController controller;

    @Test
    void test1() {
        Assertions.assertThrows(ConstraintViolationException.class, () ->
            controller.check(null));
    }

}
