package org.xyp.sample.spring.webapi.test;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

public class TestReactive {

    @Test
    void reactTest() throws InterruptedException {
//        Flux.fromStream(()-> Stream.of(1,2,3,4,5,6,7))
            Mono.just(1)
            .map(i -> {
                System.out.println(i + " " + Thread.currentThread().getName());
                return i + 1;
            })
            .subscribeOn(Schedulers.parallel())
            .publishOn(Schedulers.immediate())
            .map(i -> {
                System.out.println(i + " " + Thread.currentThread().getName());
                return i + 1;
            })
//            .publishOn(Schedulers.single())
            .map(i -> {
                System.out.println(i + " " + Thread.currentThread().getName());
                return i + 1;
            })
                .subscribeOn(Schedulers.single())
//                .subscribe()
//                .dispose();
                .block()
                ;
//            .toStream()
//            .forEach(System.out::println)
            Thread.sleep(1500L);
        ;
    }
}
