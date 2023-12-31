package org.xyp.demo.webflux.echo.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class WebFluxEchoApplication {
    public static void main(String[] args) {
        CompletableFuture<List<String>> ids = ifhIds();


        CompletableFuture<List<String>> result = ids.thenComposeAsync(l -> {
            Stream<CompletableFuture<String>> zip =

                l.stream().map(i -> {
                    CompletableFuture<String> nameTask = ifhName(i);
                    CompletableFuture<Integer> statTask = ifhStat(i);

                    return nameTask.thenCombineAsync(statTask,
                        (name, stat) -> "Name " + name + " has stats " + stat);
                });

            List<CompletableFuture<String>> combinationList = zip.toList();

            CompletableFuture<String>[] combinationArray =
                combinationList.toArray(new CompletableFuture[combinationList.size()]);

            CompletableFuture<Void> allDone = CompletableFuture.allOf(combinationArray);
            return allDone.thenApply(v -> combinationList.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        });


        List<String> results = result.join();
        System.out.println(results);

        aaa();
//        SpringApplication.run(WebFluxEchoApplication.class);
    }

    static void aaa() {


        Flux<String> ids = ifhrIds();

        Flux<String> combinations =
            ids.flatMap(id -> {
                Mono<String> nameTask = ifhrName(id);
                Mono<Integer> statTask = ifhrStat(id);

                return nameTask.zipWith(statTask,
                    (name, stat) -> "Name " + name + " has stats " + stat);
            });

        Mono<List<String>> result = combinations.collectList();

        List<String> results = result.block();
        System.out.println(results);
    }

    private static CompletableFuture<List<String>> ifhIds() {
        return CompletableFuture.supplyAsync(() -> List.of("", "", "", "", "", "", ""));
    }

    static CompletableFuture<String> ifhName(String i) {
        return CompletableFuture.supplyAsync(() -> i + " " + Thread.currentThread().getName());
    }

    static CompletableFuture<Integer> ifhStat(String i) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(i + " " + Thread.currentThread().getName() + " ");
            return 1;

        });
    }


    private static Flux<String> ifhrIds() {
        return Flux.just("", "", "", "", "", "", "");
    }

    static Mono<String> ifhrName(String i) {
        return Mono.just(" " + Thread.currentThread().getName());
    }

    static Mono<Integer> ifhrStat(String i) {
        return Mono.fromSupplier(() -> {
            System.out.println(i + " " + Thread.currentThread().getName() + " ");
            return 1;

        });
    }
}