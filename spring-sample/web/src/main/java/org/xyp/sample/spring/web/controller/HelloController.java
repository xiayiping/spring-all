package org.xyp.sample.spring.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyp.sample.spring.web.repository.jdbc.TaskDao;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/hello")
public class HelloController {
    final TaskDao taskDao;
    final ApplicationContext applicationContext;

    @GetMapping("")
    public String hello(@AuthenticationPrincipal OAuth2User principal) {
        if (applicationContext.containsBean("dispatcherServlet")) {
            System.out.println("Running in Spring MVC mode.");
        }
        if (applicationContext.containsBean("webHandler")) {
            System.out.println("Running in Spring WebFlux mode.");
        }
        log.info("hello {} ", principal);
        return "Hello World!";
    }

    @GetMapping("/mono")
    public Mono<String> mono(@AuthenticationPrincipal OAuth2User principal) {
        return Mono.just("Hello Mono!");
    }
}
