package org.xyp.function;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Fun {

    public static <T> ExceptionalFunction<T, T> updateSelf(ExceptionalConsumer<T> consumer) {
        return obj -> {
            consumer.accept(obj);
            return obj;
        };
    }

    public static <T> Function<T, T> consumeSelf(Consumer<T> consumer) {
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

    public static <R> Optional<R> cast(Object o, Class<R> target) {
        if (null == o) {
            return Optional.empty();
        }
        if (target.isAssignableFrom(o.getClass())) {
            return Optional.of(target.cast(o));
        }
        return Optional.empty();
    }

    public static <R> Function<Object, Optional<R>> cast(Class<R> target) {
        return o -> cast(o, target);
    }

}
