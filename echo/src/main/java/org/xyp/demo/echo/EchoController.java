package org.xyp.demo.echo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/echo")
public class EchoController {

    @GetMapping("/echo")
    public String call() {
        log.info("echo from echo");
        return "echo";
    }
}
