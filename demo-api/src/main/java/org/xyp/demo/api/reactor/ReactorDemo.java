package org.xyp.demo.api.reactor;

import lombok.val;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;

public class ReactorDemo {
    public static void main(String[] args) {
        val aa = Flux.just(1, 2, 3, 4, 5, 6, 7, 8)
                .subscribe(System.out::println);
        ddd(key -> null);
        System.out.println(aa);
    }

    static void ddd(Scannable sa) {
    }

    class SS implements Scannable {

        @Override
        public Object scanUnsafe(Attr key) {
            return null;
        }
    }
    interface  BBB {}
}

