package org.xyp.demo.api.functional;

@FunctionalInterface
interface FunctionMayThrow<T, R, E extends Exception> {
    public R apply(T t) throws E;
}
