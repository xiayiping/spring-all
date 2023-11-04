package org.xyp.demo.echo.test;

//import com.tcg.paradise.common.service.document.api.PdfService;
//import com.tcghl.fileservice.client.config.ServiceClientAutoConfig;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.xyp.demo.echo.EchoController;

import java.util.List;

@Slf4j
@SpringBootTest(classes = {TestController2.UnitTestConfig.class})
class TestController2 {

    @Autowired
    EchoController controller;

//    @Autowired
//    PdfService pdfService;

    @Test
    void test1() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> controller.check(null));
    }

    @Test
    void test2() {
//        List<String> imgs64 = pdfService.convertPdfToImage64(new byte[]{1, 2, 3,});
//        Assertions.assertFalse(imgs64.isEmpty());
    }

    /**
     * the default validationConfig added
     */
    @SpringJUnitConfig
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ImportAutoConfiguration(ValidationAutoConfiguration.class)
    static class UnitTestConfig {
        @Bean
        public EchoController controller() {
            return new EchoController();
        }

    }
}
