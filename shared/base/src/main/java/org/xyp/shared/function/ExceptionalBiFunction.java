package org.xyp.shared.function;

@FunctionalInterface
public interface ExceptionalBiFunction<T, U, R> {
    R apply(T t, U u) throws Throwable;
}
