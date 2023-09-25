package org.xyp.demo.call;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
@Validated
public class IndexController {

    @Autowired
    ObservationRegistry registry;

    public IndexController() { // default constructor
    }

    @WithSpan("call index")
    @Observed(name = "pdfByStringJson",
            contextualName = "pdfGenerator",
            lowCardinalityKeyValues = {"userType", "pdf"})
    @GetMapping("/")
    public String call() {
        return "index";
    }

}
