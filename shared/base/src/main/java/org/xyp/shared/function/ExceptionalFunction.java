package org.xyp.shared.function;

@FunctionalInterface
public interface ExceptionalFunction<T, R> {
    R apply(T t) throws Throwable;
}
