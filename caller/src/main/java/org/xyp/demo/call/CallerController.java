package org.xyp.demo.call;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RestController
@RequestMapping("/caller")
public class CallerController {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public CallerController(RestTemplate template, WebClient webClient) {
        this.restTemplate = template;
        this.webClient = webClient;
    }

    @GetMapping("/call")
    public String call() {
        log.info("call from caller");
        val echo = restTemplate.getForObject("https://localhost:8092/echo/echo", String.class);
        return "call " + echo;
    }

    @GetMapping("/callAsync")
    public String callAsync() {
        log.info("call from caller");
        val echo = webClient.get().uri("/echo").retrieve().bodyToMono(String.class).block();
        return "call " + echo;
    }
}
