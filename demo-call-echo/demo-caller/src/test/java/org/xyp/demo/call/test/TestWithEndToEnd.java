package org.xyp.demo.call.test;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.xyp.demo.call.CallerController;

@SpringBootTest(
//    classes = CallerMainApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestWithEndToEnd {

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    CallerController controller;

    @Test
    void testController() {
        val hello = controller.hello();
        Assertions.assertThat(hello).isEqualTo("hello fff");
    }

    @Test
    void testIndex() {
        String index = restTemplate.getForObject("http://localhost:" + port + "/",
            String.class);
        Assertions.assertThat(index).isEqualTo("index");
    }
}
