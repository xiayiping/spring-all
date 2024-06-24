package org.xyp.function.test;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xyp.exceptions.ValidateException;
import org.xyp.function.Fun;
import org.xyp.function.FunctionException;
import org.xyp.function.ValueHolder;
import org.xyp.function.wrapper.ResultOrError;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
class ResultOrErrorTest {
    @Test
    void test1() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .getResult()
            .getOption();

        Assertions.assertThat(opt).isEmpty();
    }

    @Test
    void test2() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .getResult()
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
    }

    @Test
    void test3() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .noExMap(i -> i - 4)
            .filter(i -> i > 0)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .getOption();

        Assertions.assertThat(opt).isEmpty();
    }

    @Test
    void test4() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .noExMap(i -> i)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
    }

    @Test
    void test5() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .getOption();

        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get().getName()).isEqualTo("nn");
    }

    @Test
    void test6() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .map(r -> Fun.cast(Person.class).apply(r))
            .flatMap(i -> i)
            .getOption();
        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get().getName()).isEqualTo("nn");
    }

    @Test
    void test7() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .noExMap(Fun.cast(Integer.class))
            .flatMap(i -> i)
            .getOption();
        Assertions.assertThat(opt).isEmpty();
    }

    @Test
    void test8() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .map(i -> new Person("nn", i))
            .map(Fun.updateSelf(p -> p.setAge(77)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(77)))
            .map(Fun.updateSelf(p -> p.setAge(66)))
            .map(Fun.updateSelf(p -> Assertions.assertThat(p.getAge()).isEqualTo(66)))
            .map(r -> Fun.cast(Object.class).apply(r))
            .flatMap(i -> i)
            .getOption();
        Assertions.assertThat(opt).isNotEmpty();
        Assertions.assertThat(opt.get().getClass()).isEqualTo(Person.class);
    }

    @Test
    void test9() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .getResult(ValidateException.class, ex -> new ValidateException(ex.getMessage()));
        Assertions.assertThat(opt.getOption()).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
        Assertions.assertThat(opt.get(ex -> new RuntimeException())).isEqualTo(996);
    }

    @Test
    void test10() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .getResult();
        Assertions.assertThat(opt.getOption()).isNotEmpty();
        Assertions.assertThat(opt.getOption(ex -> new RuntimeException())).isNotEmpty();
        Assertions.assertThat(opt.get()).isEqualTo(996);
        ValueHolder<Integer> vh = new ValueHolder<>(0);
        opt.ifError(er -> vh.setValue(100));
        Assertions.assertThat(vh.value()).isZero();
        opt.specError(RuntimeException.class, ex -> new RuntimeException(ex.getMessage()));
        Assertions.assertThat(opt.getException()).isNull();
        Assertions.assertThat(opt.isSuccess()).isTrue();
    }

    @Test
    void test11() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .map(o -> o / 0)
            .getResult(ValidateException.class, ex -> new ValidateException(ex.getMessage()));
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThatThrownBy(() -> opt.getOption())
            .isInstanceOf(ValidateException.class);
        ValueHolder<Integer> vh = new ValueHolder<>(0);
        opt.ifError(er -> vh.setValue(100));
        Assertions.assertThat(vh.value()).isEqualTo(100);

        Assertions.assertThatThrownBy(() -> opt.getOption(ex -> new IllegalArgumentException()))
            .isInstanceOf(IllegalArgumentException.class);
        opt.specError(RuntimeException.class, ex -> new RuntimeException(ex.getMessage()));
        Assertions.assertThat(opt.getException()).isNotNull();
    }

    @Test
    void test12() {
        val opt = ResultOrError.on(() -> 1)
            .filter(i -> i > 0)
            .map(i -> i - 4)
            .filter(i -> i > 0)
            .map(i -> i + 100)
            .mapToOptional()
            .map(o -> o.orElse(996))
            .map(o -> o / 0)
            .getResult();
        Assertions.assertThat(opt.isSuccess()).isFalse();
        Assertions.assertThatThrownBy(() -> opt.get(ex -> new ValidateException(ex.getMessage())))
            .isInstanceOf(ValidateException.class);

        Assertions.assertThatThrownBy(opt::get)
            .isInstanceOf(RuntimeException.class)
        ;
    }

    @Test
    void test13() {
        val opt = ResultOrError.of(1)
            .noExMap(i -> i)
            .mapToOptional()
            .flatMap(o -> o)
            .filter(i -> i > 1000)
            .flatMap(Optional::ofNullable)
            .mapToOptional()
            .map(o -> o.orElse(1))
            .get();
        Assertions.assertThat(opt).isOne();
    }

    @Test
    void test14() {
        val lazy = ResultOrError.of(1)
            .noExMap(i -> i)
            .mapToOptional()
            .flatMap(o -> o)
            .map(i -> i / 0);
        Assertions.assertThatThrownBy(lazy::get)
            .isInstanceOf(ArithmeticException.class);
        ;
        Assertions.assertThatThrownBy(() -> lazy.get(IllegalArgumentException.class, ex -> new IllegalArgumentException()))
            .isInstanceOf(IllegalArgumentException.class);
        ;
    }

    @Test
    void test15() {
        val lazy = ResultOrError.of(1)
            .noExMap(i -> i)
            .mapToOptional()
            .flatMap(o -> o)
            .map(i -> i / 0);
        Assertions.assertThatThrownBy(lazy::getOption)
            .isInstanceOf(ArithmeticException.class);
        Assertions.assertThatThrownBy(() -> lazy.getOption(IllegalArgumentException.class, ex -> new IllegalArgumentException()))
            .isInstanceOf(IllegalArgumentException.class);
        ;
    }

    @Test
    void test16() {
        val o = Stream.of(new Person("n", 2))
            .map(Fun.consumeSelf(p -> p.setAge(22)))
            .findFirst().get();
        Assertions.assertThat(o.getAge()).isEqualTo(22);

        val castResult = Fun.cast(null, Object.class);
        Assertions.assertThat(castResult).isEmpty();
    }

    @Test
    void test17() {
        Assertions.assertThat(new FunctionException()).isNotNull();
        Assertions.assertThat(new FunctionException("")).isNotNull();
        Assertions.assertThat(new FunctionException("", new RuntimeException())).isNotNull();
        Assertions.assertThat(new FunctionException(new RuntimeException())).isNotNull();
    }

    @Data
    static
    class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

}
