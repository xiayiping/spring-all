package org.xyp.shared.function;

@FunctionalInterface
public interface ExceptionalRunnable {
    void run() throws Throwable;
}
