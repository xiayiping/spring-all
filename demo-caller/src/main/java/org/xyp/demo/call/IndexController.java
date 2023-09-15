package org.xyp.demo.call;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
@Validated
public class IndexController {


    public IndexController() { // default constructor
    }

    @GetMapping("/")
    public String call() {
        return "index";
    }

}
