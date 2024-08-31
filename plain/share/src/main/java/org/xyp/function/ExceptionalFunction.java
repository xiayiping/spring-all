package org.xyp.function;

@FunctionalInterface
public interface ExceptionalFunction<T, R> {
    R apply(T t) throws Throwable;
}
