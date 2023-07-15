package org.xyp.demo.call;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/caller")
public class CallerController {

    private final RestTemplate restTemplate;

    public CallerController(RestTemplate template) {
        this.restTemplate = template;
    }

    @GetMapping("/call")
    public String call() {
        log.info("call from caller");
        var echo = restTemplate.getForObject("http://localhost:8082/echo/echo", String.class);
        return "call " + echo;
    }
}
