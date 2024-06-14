package org.xyp.sample.spring.springtest;

import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/test")
public class TestController {

    final ApplicationContext applicationContext;

    public TestController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("")
    public Mono<String> test() {
        if (applicationContext.containsBean("dispatcherServlet")) {
            System.out.println("Running in Spring MVC mode.");
        }
        if (applicationContext.containsBean("webHandler")) {
            System.out.println("Running in Spring WebFlux mode.");
        }
        return Mono.just("ttt");
    }

    @GetMapping("/2")
    public String test2() {
        val a = 2;
        if (applicationContext.containsBean("dispatcherServlet")) {
            System.out.println("Running in Spring MVC mode.");
        }
        if (applicationContext.containsBean("webHandler")) {
            System.out.println("Running in Spring WebFlux mode.");
        }
        return "ttt2";
    }
}
