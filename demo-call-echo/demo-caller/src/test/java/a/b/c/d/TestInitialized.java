package a.b.c.d;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xyp.demo.call.IndexController;

@SpringBootTest
class TestInitialized {

    @Autowired
    IndexController controller;

    @Test
    void test() {
        Assertions.assertThat(controller).isNotNull();
    }

}
