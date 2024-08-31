package org.xyp.sample.spring.oauth2client;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.xyp.function.wrapper.Failure;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.function.wrapper.Success;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

class Oauth2ClientApplicationTests {

    @Test
    void contextLoads() {
        Mono.just(4)
            .filter(i -> i % 2 == 0)
            .map(ignored -> "even")
            .defaultIfEmpty("odd")
            .subscribe(System.out::println);
    }

    @Test
    void test2() {
        val sss = Mono.justOrEmpty(5)
//            .flatMap(i -> {
//                if (i % 2 == 0) {
//                    return Mono.empty();
//                } else {
//                    return Mono.just(1);
//                }
//            })

            .map(i -> {
                if (i % 2 == 0) {
                    return 0;
                } else {
                    return 1;
                }
            })
            .filter(i -> i > 0)
//            .map(i -> i / 0)
            .map(i -> i + 0)
            .map(i -> i + 0)
            .map(i -> i + 0)
            .map(i -> i + 0)
            .map(i -> i + 0)
            .map(i -> i + 0)
            .map(i -> i + 0)

            .switchIfEmpty(Mono.error(new RuntimeException("sss empty")));

        sss
            .doOnError(System.out::println)
//            .block()
            .subscribe(System.out::println);
        ;

        System.out.println(111);

    }

    @Test
    void test3() {
        org.xyp.function.wrapper.Result.success(1, null);
        val flux = Flux.fromIterable(List.of(1, 0, 9, 0, 19, 0, 29, 0))
            .map(i -> ResultOrError.on(() -> i + " | " + 10 / i))
            .map(re -> re.map(s -> s + ";").getResult());

        flux.toStream().forEach(System.out::println);

        System.out.println();

        flux.toStream().forEach(rr -> {
            switch (rr) {
                case Success<String, Throwable> ss -> System.out.println("success " + ss.value());
                case Failure<String, Throwable> ff -> System.out.println("failure " + ff.throwable());
                case null, default -> System.out.println("null");
            }
        });
    }
}
