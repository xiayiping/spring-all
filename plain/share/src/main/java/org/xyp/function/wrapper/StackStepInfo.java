package org.xyp.function.wrapper;

import java.util.Optional;

public record StackStepInfo<T>(
    StackWalker.StackFrame stackFrame,
    StackStepInfo<?> previous,
    Object input,
    T output,
    Throwable throwable,
    StackStepInfo<T> child
) {

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
}
