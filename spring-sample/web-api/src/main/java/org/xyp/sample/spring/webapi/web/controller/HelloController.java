package org.xyp.sample.spring.webapi.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyp.sample.spring.webapi.domain.task.repository.jdbc.TaskDaoJdbc;
import org.xyp.sample.spring.webapi.domain.task.service.GreetService;
//import org.xyp.sample.spring.web.service.GreetService;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/hello")
public class HelloController {
    final TaskDaoJdbc taskDaoJdbc;
    final GreetService greetService;
    final ApplicationContext applicationContext;

    @GetMapping("")
    public Map<String, Object> hello(Authentication authentication) {
        if (applicationContext.containsBean("dispatcherServlet")) {
            System.out.println("Running in Spring MVC mode.");
        }
        if (applicationContext.containsBean("webHandler")) {
            System.out.println("Running in Spring WebFlux mode.");
        }
        log.info("hello {} ", authentication);
        return greetService.greet();
//        return Map.of("hello", "someone");
    }

}
