package org.xyp.demo.api.functional;

import lombok.val;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;

public abstract class Try {

    public static <R> HasResultTry<R> on(SupplierMayThrow<R, Exception> s) {
        return new HasResultTry<>(s);
    }

    public static NoResultTry on(RunnableMayThrow<Exception> s) {
        return new NoResultTry(s);
    }

}

class HasResultTry<R> {
    private final SupplierMayThrow<R, Exception> supplier;

    public HasResultTry(SupplierMayThrow<R, Exception> supplier) {
        this.supplier = supplier;
    }

    public <R1> HasResultTry<R1> mapLazy(FunctionMayThrow<R, R1, Exception> mapper) {
        return new HasResultTry<>(() -> {
            val input = supplier.apply();
            return mapper.apply(input);
        });
    }

    public R getOrThrow() throws Exception {
        return supplier.apply();
    }

    public R getOrHandle(Function<Exception, R> consumer) {
        try {
            return supplier.apply();
        } catch (Exception e) {
            return consumer.apply(e);
        }
    }
}

class NoResultTry {
    private final RunnableMayThrow<Exception> r;

    public NoResultTry(RunnableMayThrow<Exception> r) {
        this.r = r;
    }

    public NoResultTry thenLazy(RunnableMayThrow<Exception> run) {
        return new NoResultTry(() -> {
            r.run();
            run.run();
        });
    }

    public <R> HasResultTry<R> supplyLazy(SupplierMayThrow<R, Exception> supplier) {
        return new HasResultTry<>(() -> {
            r.run();
            return supplier.apply();
        });
    }
}

class TestTry {
    Callable<Void> callable = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            return null;
        }
    };


    public static void main(String[] args) throws Exception {
//        Optional.of(new FileInputStream("/"))
//            .map(s -> System.currentTimeMillis())
//            .filter(i -> i % 2 == 0)
//            .ifPresent(System.out::println);

        LongUnaryOperator f = (i -> i + 0L);

        val resultCompose = f.compose((long i) -> i * 3)
            .compose((long i) -> i + 2)
            .applyAsLong(3L);
        System.out.println(resultCompose);

        val result = Try.on(() -> 2)
            .mapLazy(i -> i / 0)
            .mapLazy(i -> i + 2)
            .getOrHandle(e -> {
                if (e instanceof ArithmeticException arth) {
                    throw new IllegalArgumentException(arth);
                } else {
                    throw new IllegalArgumentException("unknown");
                }
            });
        System.out.println(result);
    }
}
