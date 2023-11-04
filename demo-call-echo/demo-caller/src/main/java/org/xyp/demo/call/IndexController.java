package org.xyp.demo.call;

import io.micrometer.observation.annotation.Observed;
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

    @Observed(name = "pdfByStringJson",
            contextualName = "pdfGenerator",
            lowCardinalityKeyValues = {"userType", "pdf"})
    @GetMapping("/")
    public String call() {
        return "index";
    }

}
