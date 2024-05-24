package org.xyp.spiring.caller.log4j2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DemoController {
    @GetMapping("/")
    public String root() {
        return "root";
    }
}
