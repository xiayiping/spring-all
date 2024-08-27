package org.xyp.sample.spring.webvaadin.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyp.sample.spring.webvaadin.domain.HttpResp;
import org.xyp.sample.spring.webvaadin.domain.Person;

@Slf4j
@RestController
@RequestMapping("/api/v1/hello")
public class HelloController {

    @GetMapping("")
    public HttpResp<Person> hello() {
        // in order to support vaadin call , I use HttpResp and wrap data and problem in side.
        // so that there'll be uniformed response for all kind of http status.
        if (System.currentTimeMillis() % 2 == 0) {
            log.error("hahahah error");
            val detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            detail.setTitle("Error");
            detail.setDetail("Error message");
            log.error(detail.toString());
            return HttpResp.ofError(
                null,
                detail
            );
        }
        return HttpResp.ofData(
            new Person(Person.PersonId.of(111), "xyp")
        )
        ;
    }
}
