package org.xyp.shared.function;

@FunctionalInterface
public interface ExceptionalBiConsumer<S, T> {
    void accept(S s, T t) throws Throwable;
}

