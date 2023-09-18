package org.xyp.demo.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {
    @GetMapping("ping")
    public String ping() {
        return "ping";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
