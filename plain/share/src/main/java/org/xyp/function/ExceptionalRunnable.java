package org.xyp.function;

@FunctionalInterface
public interface ExceptionalRunnable {
    void run() throws Exception;
}
