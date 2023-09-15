package org.xyp.demo.echo;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/echo")
@Validated
public class EchoController {

    public EchoController() {
        log.info("EchoController created");
    }

    private Random random = new Random();

    @GetMapping("/echo")
    public String call() {
        log.info("echo from echo");
        return "echo " + random.nextInt(100) + (3/0);
    }

    @GetMapping("/nullcheck")
    public String check(@NotNull String title) {
        return "hello " + title;
    }

}
