package org.xyp.demo.api.functional;

@FunctionalInterface
public interface RunnableMayThrow<E extends Exception> {
    public void run() throws E;
}
