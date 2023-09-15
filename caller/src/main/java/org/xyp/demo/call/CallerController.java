package org.xyp.demo.call;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.xyp.demo.api.EchoService;

import java.util.Base64;

@Slf4j
@Data
@RestController
@AllArgsConstructor
@RequestMapping("/caller")
@Validated
public class CallerController {

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final EchoService echoService;

    @Operation
    @GetMapping("/echo")
    public String echo() {
        log.info("call from caller");
        val echo = restTemplate.getForObject("/echo/echo", String.class);
        return "echo RestTemplate " + echo;
    }

    @Operation
    @GetMapping("/echoAsync")
    public String echoAsync() {
        log.info("call from caller");
        val echo = webClient.get().uri("/echo/echo").retrieve().bodyToMono(String.class).block();
        return "echo webClient " + echo;
    }

    @Operation
    @GetMapping("/echoFeign")
    public String echoFeign() {
        log.info("echo by Feign");
        val echo = echoService.echo();
        return "echo Feign " + echo;
    }

    @Operation
    @GetMapping("/hello")
    public String hello() {
        log.info("call from hello");
        return "hello fff";
    }

    @Operation
    @GetMapping("/nullcheck")
    public String check(@NotNull String title) {
        return "hello " + title;
    }


    @Operation
    @GetMapping("/getBytes")
    public String getBytes() {
        return Base64.getEncoder().encodeToString(new byte[]{1, 2, 4, 4, 5});
    }
}
