package org.xyp.demo.otel;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("")
public class IndexController {

    @GetMapping("/")
    public String index() {
        log.debug("call index");
        log.info("call index");
        log.warn("call index");
        log.error("call index");
        return "index page";
    }

    @GetMapping("/hello")
    public String hello() {

        log.debug("call hello");
        log.info("call hello");
        log.warn("call hello");
        log.error("call hello");
        return "hello page";
    }
}
