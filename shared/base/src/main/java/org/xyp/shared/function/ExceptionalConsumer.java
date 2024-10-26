package org.xyp.shared.function;

@FunctionalInterface
public interface ExceptionalConsumer<T> {
    void accept(T t) throws Throwable;
}
