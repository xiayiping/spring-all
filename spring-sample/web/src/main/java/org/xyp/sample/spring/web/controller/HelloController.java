package org.xyp.sample.spring.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyp.sample.spring.web.repository.jdbc.TaskDao;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/hello")
public class HelloController {
    final TaskDao taskDao;

    @GetMapping("")
    public String hello(@AuthenticationPrincipal OAuth2User principal) {
        log.info("hello {} ", principal);
        return "Hello World!";
    }
}
