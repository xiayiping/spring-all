package org.xyp.demo.webflux.echo.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public Mono<String> index(ServerWebExchange exchange) {
//        val headers = exchange.getRequest().getHeaders();
//        headers.forEach((k, list) -> System.out.println(k + " : " + list));
//        exchange.getAttributes().entrySet().forEach(System.out::println);
//        log.info("index {}" , System.currentTimeMillis());
        return Mono.just("index " + System.currentTimeMillis());
    }

    @GetMapping("1")
    public String index1() {
        return "index1 " + System.currentTimeMillis();
    }
}
