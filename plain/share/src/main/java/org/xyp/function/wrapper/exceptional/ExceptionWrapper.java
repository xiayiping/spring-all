package org.xyp.function.wrapper.exceptional;

import java.util.function.Function;

public record ExceptionWrapper<E>(
    Class<E> exceptionClass,
    Function<Exception, E> wrapper
) {
    public E wrap(Exception error) {
        try {
            return exceptionClass.cast(error);
        } catch (Exception e) {
            return wrapper.apply(error);
        }
    }

    public static <E extends Exception> ExceptionWrapper<E> of(Class<E> exceptionClass, Function<Exception, E> wrapper) {
        return new ExceptionWrapper<>(exceptionClass, wrapper);
    }
}
