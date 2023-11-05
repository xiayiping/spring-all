package org.xyp.demo.call.test;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.xyp.demo.call.CallerController;
import org.xyp.demo.call.IndexController;

import static org.hamcrest.Matchers.containsString;

@WebMvcTest(
    controllers = {
        IndexController.class,
        CallerController.class
    }) // only specified controllers will be created
@ActiveProfiles("test")
class TestWithMockMvc {

    @Autowired
    private MockMvc mvc;

    @MockBean // this bean will be injected to controller
    RestTemplate restTemplate;

    @MockBean // this bean will be injected to controller
    WebClientSsl webClientSsl;

    @Test
    void testIndex() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpectAll(
                MockMvcResultMatchers.content().string("index"),
                MockMvcResultMatchers.status().isOk()
            );
    }

    @Test
    void testMockEcho() throws Exception {
        Mockito.when(
                restTemplate.getForObject(Mockito.anyString(), Mockito.any(Class.class)))
            .thenReturn("echo mock");
        this.mvc.perform(MockMvcRequestBuilders.get("/echo"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpectAll(
                MockMvcResultMatchers.content().string(containsString("echo mock")),
                MockMvcResultMatchers.status().isOk()
            );
        ;
    }
}
