package org.xyp.function.wrapper;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class StackStepInfo<T> {
    private final StackWalker.StackFrame stackFrame;
    private final StackStepInfo<?> previous;
    private final Object input;
    private final T output;
    private final Throwable throwable;
    private final StackStepInfo<T> child;

    public boolean isError() {
        return null != throwable;
    }

    public Optional<StackStepInfo<T>> getChild() {
        return Optional.ofNullable(child);
    }

    public StackStepInfo(
        StackWalker.StackFrame stackFrame,
        StackStepInfo<?> previous,
        Object input,
        T output,
        Throwable throwable
    ) {
        this(stackFrame, previous, input, output, throwable, null);
    }

    public StackWalker.StackFrame stackFrame() {
        return stackFrame;
    }

    public StackStepInfo<?> previous() {
        return previous;
    }

    public Object input() {
        return input;
    }

    public T output() {
        return output;
    }

    public Throwable throwable() {
        return throwable;
    }

    public StackStepInfo<T> child() {
        return child;
    }
}
