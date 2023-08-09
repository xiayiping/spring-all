package org.xyp.demo.call;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RestController
@RequestMapping("/caller")
@Validated
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
        val echo = restTemplate.getForObject("https://localhost:8094/echo/echo", String.class);
        return "call " + echo;
    }

    @GetMapping("/callAsync")
    public String callAsync() {
        log.info("call from caller");
        val echo = webClient.get().uri("/echo").retrieve().bodyToMono(String.class).block();
        return "call " + echo;
    }

    @GetMapping("/hello")
    public String hello() {
        log.info("call from hello");
        return "hello ";
    }

    @GetMapping("/nullcheck")
    public String check(@NotNull String title) {
        return "hello " + title;
    }

}
