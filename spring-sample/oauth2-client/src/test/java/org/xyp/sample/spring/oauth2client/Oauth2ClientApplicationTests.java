package org.xyp.sample.spring.oauth2client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

class Oauth2ClientApplicationTests {

    @Test
    void contextLoads() {
        Mono.just(4)
            .filter(i -> i % 2 == 0)
            .map(ignored -> "even")
            .defaultIfEmpty("odd")
            .subscribe(System.out::println);
    }

}
