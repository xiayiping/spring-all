package org.xyp.demo.webflux.echo.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public Mono<String> index() {
        return Mono.just("index " + System.currentTimeMillis());
    }

    @GetMapping("1")
    public String index1() {
        return "index1 " + System.currentTimeMillis();
    }
}
