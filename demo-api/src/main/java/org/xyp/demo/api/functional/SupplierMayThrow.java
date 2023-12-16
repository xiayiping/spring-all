package org.xyp.demo.api.functional;

@FunctionalInterface
public interface SupplierMayThrow<R, E extends Exception> {
    public R apply() throws E;
}
