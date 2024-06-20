package org.xyp.function;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Fun {
    public static <T> T update(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }

    public static <T> ExceptionalSupplier<T> updateWithConsumer(T t, Consumer<T> consumer) {
        return () -> {
            consumer.accept(t);
            return t;
        };
    }

    public static <T> ExceptionalFunction<T, T> selfMap(ExceptionalConsumer<T> consumer) {
        return obj -> {
            consumer.accept(obj);
            return obj;
        };
    }

    public static <E extends RuntimeException> E convertRte(Exception e, Class<E> target, Function<Exception, E> converter) {
        if (target.isAssignableFrom(e.getClass())) {
            return (target.cast(e));
        }
        return converter.apply(e);
    }
}
