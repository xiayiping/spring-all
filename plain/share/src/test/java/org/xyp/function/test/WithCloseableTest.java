package org.xyp.function.test;


import lombok.Getter;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xyp.function.Fun;
import org.xyp.function.ValueHolder;
import org.xyp.function.wrapper.WithCloseable;

import java.io.Closeable;

class WithCloseableTest {
    @Test
    void test1() {
        ValueHolder<MockCloseable> holder = ValueHolder.of(null);
        val lazy = WithCloseable.open(MockCloseable::new)
            .map(Fun.updateSelf(holder::setValue))
            .map(c -> 1);
        ;
        Assertions.assertThat(lazy.closeAndGet()).isOne();
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isTrue();

        Assertions.assertThat(lazy.closeAndGet()).isOne();
        Assertions.assertThat(lazy.closeAndGet(IllegalArgumentException.class, IllegalArgumentException::new)).isOne();

        Assertions.assertThat(holder.value().isClosed()).isTrue();
    }

    @Test
    void test2() {
        val lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c / 0);

        Assertions.assertThatThrownBy(lazy::closeAndGet).isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.closeAndGet(IllegalArgumentException.class, IllegalArgumentException::new)).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isFalse();
    }

    @Test
    void test3() {
        val lazy = WithCloseable.open(MockCloseableErr::new)
            .map(c -> 1);

        Assertions.assertThatThrownBy(lazy::closeAndGet).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isFalse();
    }

    @Test
    void test4() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>(null);
        val lazy = WithCloseable.open(MockCloseable::new)
            .map(Fun.updateSelf(holder::setValue))
            .map(c -> 1)
            .consume(i -> {
            })
            .map(c -> null)
            .consume(i -> {
            })
            .mapWithCloseable((c, i) -> i)
            .map(c -> 11);
        ;
        Assertions.assertThat(lazy.closeAndGet()).isNull();
    }

    @Test
    void test5() {
        val lazy = WithCloseable.open(MockCloseableErr2::new)
            .map(c -> 1);

        Assertions.assertThatThrownBy(lazy::closeAndGet).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isFalse();
    }

    @Getter
    static class MockCloseable implements AutoCloseable {
        boolean closed = false;

        @Override
        public void close() throws Exception {
            this.closed = true;
        }
    }

    @Getter
    static class MockCloseableErr implements AutoCloseable {

        MockCloseableErr() {
            throw new RuntimeException();
        }

        @Override
        public void close() throws Exception {
        }
    }

    @Getter
    static class MockCloseableErr2 implements AutoCloseable {

        MockCloseableErr2() {
        }

        @Override
        public void close() throws Exception {
            throw new Exception();
        }
    }
}
