package org.xyp.demo.echo.check;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.DoubleAssert;
import org.assertj.core.api.PredicateAssert;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.springframework.util.ConcurrentLruCache;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CheckAqs {

    @Test
    void checkLock() {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.unlock();
        double av = 1.1D;
        Assertions.assertThat(av).isCloseTo(1.1, Offset.offset(1e-4));
    }

    @Test
    void checkConcurrencyMap() {
        ConcurrentHashMap<Integer, String> chm = new ConcurrentHashMap<>();
        chm.put(1, "1");
        chm.put((1 << 4) + 1, "2");
        chm.put((2 << 4) + 1, "12");
        chm.put((3 << 4) + 1, "22");
        chm.put((4 << 4) + 1, "32");
        chm.put((5 << 4) + 1, "42");
        chm.put((6 << 4) + 1, "52");
        chm.put((7 << 4) + 1, "62");
        chm.put((8 << 4) + 1, "72");
        chm.put((9 << 4) + 1, "82");
        chm.put((10 << 4) + 1, "92");
        chm.put((11 << 4) + 1, "02");
        chm.put((12 << 4) + 1, "112");
        chm.put((13 << 4) + 1, "122");

        chm.remove(13 << 4);
        Assertions.assertThat(chm).isNotNull();


        val res = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).reduce((integer, integer2) -> {
            System.out.println(integer + " " + integer2);
            return integer + integer2;
        }).orElseThrow();
        val integerMap = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).collect(Collectors.groupingBy(integer -> integer % 2));
        System.out.println(res);
        System.out.println(integerMap);

        ConcurrentLruCache<String, String> lru = new ConcurrentLruCache<>(4, s -> "default");
        lru.get("aa");
        lru.get("aa");
        Assertions.assertThat(lru).isNotNull();
    }
}
