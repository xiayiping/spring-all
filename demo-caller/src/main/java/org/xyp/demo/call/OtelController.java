package org.xyp.demo.call;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/")
public class OtelController {

    private RestTemplate restTemplate;

    public OtelController(
            @Qualifier("otel") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/otel")
    public String otel() {
        val forObject = restTemplate.getForObject("http://127.0.0.1:8080", String.class);
        return "get result from otel " + forObject;
    }

    private String additional() {
        return "additional";
    }
}
