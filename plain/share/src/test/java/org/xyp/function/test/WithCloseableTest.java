package org.xyp.function.test;


import lombok.Getter;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xyp.function.Fun;
import org.xyp.function.ValueHolder;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.function.wrapper.StackStepInfo;
import org.xyp.function.wrapper.WithCloseable;

import java.io.Closeable;

class WithCloseableTest {
    @Test
    void test1() {
        ValueHolder<MockCloseable> holder = ValueHolder.of(null);
        Assertions.assertThat(holder.isEmpty()).isTrue();
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(Fun.updateSelf(holder::setValue))
            .map(c -> 1);
        ;
        Assertions.assertThat(lazy.closeAndGet()).isOne();
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isTrue();
        Assertions.assertThat(lazy.closeAndGetResult().getOrFallBackForError(ex -> 2)).isOne();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isTrue();

        Assertions.assertThat(lazy.closeAndGet()).isOne();
        Assertions.assertThat(lazy.closeAndGet(IllegalArgumentException.class, IllegalArgumentException::new)).isOne();

        Assertions.assertThat(holder.value().isClosed()).isTrue();
    }

    @Test
    void test2() {
        var lazy = WithCloseable.open(MockCloseable::new)
            .map(c -> 1)
            .map(c -> c / 0);

        Assertions.assertThatThrownBy(() -> lazy.closeAndGet()).isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.closeAndGet(IllegalArgumentException.class, IllegalArgumentException::new)).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isFalse();
    }

    @Test
    void test3() {
        var lazy = WithCloseable.open(MockCloseableErr::new)
            .map(c -> 1);

        Assertions.assertThatThrownBy(() -> lazy.closeAndGet()).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isFalse();
    }

    @Test
    void test4() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>(null);
        var lazy = WithCloseable.open(MockCloseable::new)
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
        Assertions.assertThat(lazy.logTrace(System.out::println).closeAndGet()).isNull();
    }

    @Test
    void test5() {
        var lazy = WithCloseable.open(MockCloseableErr2::new)
            .map(c -> 1);

        Assertions.assertThatThrownBy(() -> lazy.closeAndGet()).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(lazy.closeAndGetResult().isSuccess()).isFalse();
        Assertions.assertThat(lazy.closeAndGetResult().getOrFallBackForError(ex -> 55)).isEqualTo(55);
        Assertions.assertThat(lazy.closeAndGetResult(IllegalArgumentException.class, IllegalArgumentException::new).isSuccess()).isFalse();
    }

    @Test
    void test6() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>(null);
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(Fun.updateSelf(holder::setValue))
            .mapWithCloseable((c, v) -> {
                holder.setValue(c);
                Assertions.assertThat(holder.value().isClosed()).isFalse();
                return v;
            })
            .map(c -> 1)
            .fallBackEmpty(c -> 49)
            .consume(i -> {
            })
            .map(c -> null)
            .consume(i -> {
            })
            .mapWithCloseable((c, i) -> i)
            .map(c -> 11)
            .fallBackEmpty(c -> 44);
        ;
        Assertions.assertThat(lazy.closeAndGet()).isEqualTo(44);
        Assertions.assertThat(errorHolder.value()).isZero();
        Assertions.assertThat(finallyHolder.value()).isOne();
        Assertions.assertThat(holder.value().isClosed()).isTrue();
    }

    @Test
    void test7() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(Fun.updateSelf(holder::setValue))
            .mapWithCloseable((c, v) -> {
                holder.setValue(c);
                Assertions.assertThat(holder.value().isClosed()).isFalse();
                return v;
            })
            .map(c -> 1)
            .fallBackEmpty(c -> 49)
            .logTrace(System.out::println)
            .consume(i -> {
                Assertions.assertThat(i).isOne();
                throw new RuntimeException("run time e");
            })
            .map(i -> i)
            .fallBackEmpty(c -> 44)
            .logTrace(System.out::println);

        Assertions.assertThat(errorHolder.value()).isZero();
        val result = lazy.closeAndGetResult();
        Assertions.assertThat(result).matches(r -> !r.isSuccess());
        Assertions.assertThat(errorHolder.value()).isOne();
        Assertions.assertThat(finallyHolder.value()).isOne();
        Assertions.assertThat(holder.value().isClosed()).isTrue();

        final ValueHolder<Integer> stackSize = new ValueHolder<>(0);
        var stackOpt = result.getStackStepInfo();
        System.out.println("--------");
        System.out.println("--------");
        Assertions.assertThat(stackOpt).isNotEmpty();
        stackOpt.ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            while (null != current) {
                stackSize.setValue(stackSize.value() + 1);
                System.out.println(current.stackFrame());
                System.out.println("    " + current.input());
                System.out.println("    " + current.output());
                current = current.previous();
            }
        });
        Assertions.assertThat(stackSize.value()).isGreaterThan(4);
    }

    @Test
    void test08() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .filter(i -> i > 100)
            .fallBackEmpty(c -> 49 / 0)
            .logTrace(System.out::println)
            .logTrace(System.out::println, false)
            .logTrace(System.out::println, () -> false)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
        lazy.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 5)
                    Assertions.assertThat(current.throwable()).isNotNull();
                else
                    Assertions.assertThat(current.throwable()).isNull();
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test09() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .mapWithCloseable((c, i) -> i / 0)
            .mapWithCloseable((c, i) -> i / 0)
            .filter(i -> i > 100)
            .fallBackEmpty(c -> 49 / 0)
            .logTrace(System.out::println)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
        lazy.getStackStepInfo().ifPresent(stack -> {
            StackStepInfo<?> current = stack;
            var num = 6;
            while (current != null) {
                if (num >= 5)
                    Assertions.assertThat(current.throwable()).isNotNull();
                else
                    Assertions.assertThat(current.throwable()).isNull();
                num--;
                current = current.previous();
            }
        });
    }

    @Test
    void test10() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .flatMap(i -> ResultOrError.on(() -> i + 2)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3)
            )
            .filter(i -> i > 100)
            .fallBackEmpty(c -> 49 / 0)
            .logTrace(System.out::println)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
    }

    @Test
    void test11() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .flatMap(i -> ResultOrError.on(() -> i + 2)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3 / 0)
                .map(t -> t + 3)
            )
            .filter(i -> i > 100)
            .logTrace(System.out::println)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
    }

    @Test
    void test12() {
        ValueHolder<MockCloseable> holder = new ValueHolder<>();
        ValueHolder<Integer> errorHolder = new ValueHolder<>(0);
        ValueHolder<Integer> finallyHolder = new ValueHolder<>(0);
        val lazy = WithCloseable.open(
                MockCloseable::new,
                (c, err) -> errorHolder.setValue(errorHolder.value() + 1),
                closeable -> finallyHolder.setValue(finallyHolder.value() + 1)
            )
            .map(c -> 1)
            .map(i -> i + 1)
            .map(t -> t + 3 / 0)
            .flatMap(i -> ResultOrError.on(() -> i + 2)
                .map(t -> t + 3)
                .map(t -> t + 3)
                .map(t -> t + 3 / 0)
                .map(t -> t + 3)
            )
            .filter(i -> i > 100)
            .logTrace(System.out::println)
            .map(i -> 99)
            .closeAndGetResult()
            //
            ;
        Assertions.assertThat(lazy.isSuccess()).isFalse();
        Assertions.assertThat(lazy.getStackStepInfo()).isNotEmpty();
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
