package org.xyp.function.wrapper;

import org.xyp.function.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * map 函数为递归 lazy 调用，可以保留链式调用的调用栈<br/>
 * 返回值一旦有null则调用链中断<br/>
 * 由于是 lazy 调用， 因此最后需要用 getXXX 函数做真正的调用
 *
 * @param <R>
 */
public class ResultOrError<R> {

    private final Supplier<StackStepInfo<R>> supplier;

    private ResultOrError(Supplier<StackStepInfo<R>> supplier) {
        this.supplier = supplier;
    }

    static StackWalker.StackFrame getStackStep() {
        return StackWalker.getInstance()
            .walk(stream -> stream.filter(s -> !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName()))
                .findFirst())
            .orElse(null);
    }

    public static <T1> ResultOrError<T1> of(T1 t1) {
        final var frame = getStackStep();
        return new ResultOrError<>(() -> new StackStepInfo<>(frame, null, null, t1, null));
    }

    public static <R> ResultOrError<R> on(ExceptionalSupplier<R> supplier) {
        final var frame = getStackStep();
        return new ResultOrError<>(() -> {
            try {
                final var result = supplier.get();
                return new StackStepInfo<>(frame, null, null, result, null);
            } catch (Throwable throwable) {
                return new StackStepInfo<>(frame, null, null, null, throwable);
            }
        });
    }

    public static ResultOrError<Void> doRun(ExceptionalRunnable runner) {
        final var frame = getStackStep();
        return new ResultOrError<>(() -> {
            try {
                runner.run();
                return new StackStepInfo<>(frame, null, null, null, null);
            } catch (Throwable throwable) {
                return new StackStepInfo<>(frame, null, null, null, throwable);
            }
        }
        );
    }

    public ResultOrError<R> filter(Predicate<? super R> predicate) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                return getStackStepInfoByFilter(predicate, prevStack, frame);
            }
        );
    }

    static <R> StackStepInfo<R> getStackStepInfoByFilter(Predicate<? super R> predicate, StackStepInfo<R> prevStack, StackWalker.StackFrame frame) {
        if (prevStack.isError()) {
            return prevStack;
        }
        final var lastOutput = prevStack.output();
        try {
            if (null != lastOutput && predicate.test(lastOutput)) {
                return prevStack;
            } else {
                return new StackStepInfo<>(frame, prevStack, null, null, null);
            }
        } catch (Throwable throwable) {
            return new StackStepInfo<>(frame, prevStack, lastOutput, null, throwable);
        }
    }

    public ResultOrError<R> fallbackForEmpty(Supplier<R> emptySupplier) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                final var lastOutput = prevStack.output();
                if (prevStack.isError()) {
                    return prevStack;
                }
                try {
                    if (null == lastOutput) {
                        var currentRes = emptySupplier.get();
                        return new StackStepInfo<>(frame, prevStack, null, currentRes, null);
                    } else {
                        return new StackStepInfo<>(frame, prevStack, lastOutput, lastOutput, null);
                    }
                } catch (Throwable throwable) {
                    return new StackStepInfo<>(frame, prevStack, lastOutput, null, throwable);
                }
            }
        );
    }

    public ResultOrError<R> consume(ExceptionalConsumer<? super R> consumer) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var prevStack = supplier.get();
                return getStackByConsume(consumer, prevStack, frame);
            }
        );
    }

    static <R> StackStepInfo<R> getStackByConsume(ExceptionalConsumer<? super R> consumer, StackStepInfo<R> prevStack, StackWalker.StackFrame frame) {

        final var lastOutput = prevStack.output();
        if (prevStack.isError()) {
            return prevStack;
        } else if (null != lastOutput) {
            try {
                consumer.accept(lastOutput);
                return new StackStepInfo<>(frame, prevStack, lastOutput, lastOutput, null);
            } catch (Throwable throwable) {
                return new StackStepInfo<>(frame, prevStack, lastOutput, null, throwable);
            }
        } else {
            return prevStack;
        }
    }

    public ResultOrError<R> logTrace(Consumer<String> logger) {
        return logTrace(logger, true);
    }

    public ResultOrError<R> logTrace(Consumer<String> logger, Supplier<Boolean> shouldLog) {
        return logTrace(logger, shouldLog.get());
    }

    public ResultOrError<R> logTrace(Consumer<String> logger, boolean shouldLog) {
        if(!shouldLog) {
            return this;
        }
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = this.supplier.get();
                final var lastOutput = previousStackInfo.output();
                try {
                    final var current = new StackStepInfo<R>(frame, previousStackInfo, lastOutput, lastOutput, previousStackInfo.throwable());
                    logTraceInternal(logger, current);
                    return current;
                } catch (Throwable t) {
                    return new StackStepInfo<>(frame, previousStackInfo, previousStackInfo, null, t);
                }
            }
        );
    }

    private static final String TRACE_LOG_INDENT = "    ";

    static void logTraceInternal(Consumer<String> logger, StackStepInfo<?> stackInfo) {
        logTraceInternal(logger, stackInfo, "");
    }

    private static void logTraceInternal(Consumer<String> logger, StackStepInfo<?> stackInfo, String prefix) {
        var currentStackInfo = stackInfo;
        while (null != currentStackInfo) {
            final var frame = currentStackInfo.stackFrame().toString();
            final var input = Optional.of(Objects.toString(currentStackInfo.input()))
                .map(s -> {
                    if (s.length() > 100) {
                        return s.substring(0, 100) + " ...";
                    }
                    return s;
                }).orElse(null);
            final var output = Optional.of(Objects.toString(currentStackInfo.output()))
                .map(s -> {
                    if (s.length() > 100) {
                        return s.substring(0, 100) + " ...";
                    }
                    return s;
                }).orElse(null);
            if (stackInfo.isError()) {
                final var log = String.format("%s%s\n    %sinput: %s\n    %soutput: %s\n    %sexception: %s",
                    prefix, frame,
                    prefix, input,
                    prefix, output,
                    prefix, Optional.ofNullable(currentStackInfo.throwable()).map(Object::toString).orElse("")
                );
                logger.accept(log);
            } else {
                final var log = String.format("%s%s\n    %sinput: %s\n    %soutput: %s",
                    prefix, frame,
                    prefix, input,
                    prefix, output
                );
                logger.accept(log);
            }

            currentStackInfo.getChild().ifPresent(child -> {
                logTraceInternal(logger, child, prefix + TRACE_LOG_INDENT);
            });

            currentStackInfo = currentStackInfo.previous();
        }
    }

    @SuppressWarnings("unchecked")
    public <U> ResultOrError<U> map(ExceptionalFunction<? super R, ? extends U> mapper) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = supplier.get();
                return getStackStepInfoByMapper(mapper, previousStackInfo, frame);
            }
        );
    }

    @SuppressWarnings("unchecked")
    static <R, U> StackStepInfo<U> getStackStepInfoByMapper(
        ExceptionalFunction<? super R, ? extends U> mapper,
        StackStepInfo<R> previousStackInfo,
        StackWalker.StackFrame frame
    ) {
        final var lastOutput = previousStackInfo.output();
        if (previousStackInfo.isError()) {
            return (StackStepInfo<U>) previousStackInfo;
        } else if (null != lastOutput) {
            try {
                final var mappedVal = mapper.apply(lastOutput);
                return new StackStepInfo<>(frame, previousStackInfo, lastOutput, mappedVal, null);
            } catch (Throwable t) {
                return new StackStepInfo<>(frame, previousStackInfo, lastOutput, null, t);
            }
        } else {
            return (StackStepInfo<U>) previousStackInfo;
        }
    }

    public <U> ResultOrError<U> noExMap(Function<? super R, ? extends U> mapper) {
        return map(mapper::apply);
    }

    @SuppressWarnings("unchecked")
    public <U> ResultOrError<U> flatMap(Function<? super R, ResultOrError<U>> mapper) {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = supplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfo<U>) previousStackInfo;
                } else if (null != lastOutput) {
                    final var mappedResult = mapper.apply(lastOutput).getResult();
                    final var childStack = mappedResult.getStackStepInfo();
                    if (mappedResult.isSuccess()) {
                        final var mappedVal = mappedResult.get();
                        return new StackStepInfo<>(frame, previousStackInfo, lastOutput, mappedVal, null, childStack.orElse(null));
                    } else {
                        return new StackStepInfo<>(frame, previousStackInfo, lastOutput, null, mappedResult.getError(), childStack.orElse(null));
                    }
                } else {
                    return (StackStepInfo<U>) previousStackInfo;
                }
            }
        );
    }

    public R get() {
        try {
            final var res = supplier.get();
            if (res.isError()) {
                throw res.throwable();
            }
            return res.output();
        } catch (Throwable e) {
            throw Fun.convertRte(e, RuntimeException.class, FunctionException::new);
        }
    }

    public Optional<R> getOption() {
        return Optional.ofNullable(get());
    }

    public <E extends RuntimeException> R getOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        try {
            final var res = supplier.get();
            if (res.isError()) {
                throw res.throwable();
            }
            return res.output();
        } catch (Throwable e) {
            throw Fun.convertRte(e, target, exceptionMapper);
        }
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        return Optional.ofNullable(getOrSpecError(target, exceptionMapper));
    }

    public Result<R, Throwable> getResult() {
        final var res = (supplier.get());
        if (res.isError()) {
            return Result.failure(res.throwable(), res);
        }
        return Result.success(res.output(), res);
    }

    public <E extends RuntimeException> Result<R, E> getResultOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        final var res = (supplier.get());
        if (res.isError()) {
            return Result.failure(Fun.convertRte(res.throwable(), target, exceptionMapper), res);
        }
        return Result.success(res.output(), res);
    }
}
