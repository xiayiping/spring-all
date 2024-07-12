package org.xyp.sample.spring.webapijwt.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.xyp.sample.spring.webapijwt.WebApiJwtApplication.CONTEXT;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(CONTEXT + "/hello")
public class HelloController {
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
        return Map.of("message", "Hello " + authentication);
    }

}
