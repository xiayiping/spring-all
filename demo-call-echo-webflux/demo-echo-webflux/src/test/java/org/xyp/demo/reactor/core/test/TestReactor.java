package org.xyp.demo.reactor.core.test;

import lombok.val;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

class TestReactor {

    @Test
    void testBasic() {
        val map = Flux.just(1, 2, 3, 4, 5)
            .map(i -> {
                System.out.println("mapping " + i);
                return i * 2;
            });

        System.out.println(map);

        map.subscribe(i -> System.out.println(i + " " + Thread.currentThread().getName()))


            .dispose();
        ;
    }
    interface MyEventListener<T> {
        void onDataChunk(List<T> chunk);
        void processComplete();
    }

    @Test
    void testSchedularClock() {
        Flux<String> bridge = Flux.create(sink -> new MyEventListener<String>() {

            public void onDataChunk(List<String> chunk) {
                for(String s : chunk) {
                    sink.next(s);
                }
            }

            public void processComplete() {
                sink.complete();
            }
        });
    }
}
