package org.xyp.function;

@FunctionalInterface
public interface ExceptionalConsumer<T> {
    void accept(T t) throws Exception;
}
