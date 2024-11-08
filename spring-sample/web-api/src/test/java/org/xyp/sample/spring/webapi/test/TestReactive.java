package org.xyp.sample.spring.webapi.test;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

public class TestReactive {

    @Test
    void reactTest() {
//        Flux.fromStream(()-> Stream.of(1,2,3,4,5,6,7))
            Mono.just(1)
            .map(i -> {
                System.out.println(i + " " + Thread.currentThread().getName());
                return i + 1;
            })
//            .subscribeOn(Schedulers.parallel())
//            .publishOn(Schedulers.boundedElastic())
            .map(i -> {
                System.out.println(i + " " + Thread.currentThread().getName());
                return i + 1;
            })
//            .publishOn(Schedulers.single())
            .map(i -> {
                System.out.println(i + " " + Thread.currentThread().getName());
                return i + 1;
            })
                .block()
//            .toStream()
//            .forEach(System.out::println)
        ;
    }
}
