package org.xyp.function;

@FunctionalInterface
public interface ExceptionalSupplier<R> {
    R get() throws Throwable;
}
