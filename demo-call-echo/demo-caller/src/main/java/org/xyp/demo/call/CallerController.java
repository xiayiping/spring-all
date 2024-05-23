package org.xyp.demo.call;

import io.micrometer.common.KeyValue;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/")
@Validated
public class CallerController {

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final ObservationRegistry observationRegistry;


    @Timed("echo_timer")
    @GetMapping("/echo")
    public String echo() throws Exception {
        log.info("call from caller");
        val echo = restTemplate.getForObject("/echo", String.class);
        return "echo RestTemplate " + echo + " " + adhocInfo();
    }

    public String adhocInfo() throws ExecutionException, InterruptedException {

        val fut = ContextExecutorService.wrap(ForkJoinPool.commonPool())
                .submit(() -> Observation.createNotStarted("adhoc", observationRegistry)
                        .lowCardinalityKeyValue(KeyValue.of("aaabbbccc", "ooopppqqq"))
                        .observe(() -> {
                            log.info("call from adhocInfo 999888");
                            return "adhoc";
                        }));

        return fut.get();
    }

    @GetMapping("/echoAsync")
    public String echoAsync() {
        log.info("call from caller");
        val echo =
                webClient.get().uri("/echo").retrieve().bodyToMono(String.class).block();
        return "echo webClient " + echo;
    }

    @GetMapping("/echoFeign")
    public String echoFeign() {
        log.info("echo by Feign");
        return "echo Feign " /*+ echo*/;
    }

    @GetMapping("/hello")
    public String hello() {
        log.info("call from hello");
        return "hello fff";
    }

    @GetMapping("/nullcheck")
    public String check(@NotNull String title) {
        return "hello " + title;
    }


    @GetMapping("/getBytes")
    public String getBytes() {
        return Base64.getEncoder().encodeToString(new byte[]{1, 2, 4, 4, 5});
    }
}
