package org.xyp.shared.function;

@FunctionalInterface
public interface ExceptionalSupplier<R> {
    R get() throws Throwable;
}
