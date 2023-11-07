package org.xyp.demo.echo.test;

//import com.tcg.paradise.common.service.document.api.PdfService;
//import com.tcghl.fileservice.client.config.ServiceClientAutoConfig;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.xyp.demo.echo.EchoController;
import org.xyp.demo.echo.service.UserRepository;

@Slf4j
@WebMvcTest(controllers = {EchoController.class})
class TestController2 {

    @Autowired
    private MockMvc mvc;

    @MockBean
    MeterRegistry meterRegistry;

    @MockBean
    UserRepository userRepository;

    @Autowired
    EchoController controller;

//    @Autowired
//    PdfService pdfService;

    @Test
    void test1() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> controller.check(null));
    }

    @Test
    void test2() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(MockMvcResultMatchers.status().isOk());
//        List<String> imgs64 = pdfService.convertPdfToImage64(new byte[]{1, 2, 3,});
//        Assertions.assertFalse(imgs64.isEmpty());
    }

}
