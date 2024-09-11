package org.xyp.function.wrapper;

import lombok.val;
import org.xyp.function.ExceptionalConsumer;
import org.xyp.function.ExceptionalFunction;
import org.xyp.function.ExceptionalRunnable;
import org.xyp.function.ExceptionalSupplier;

import java.util.Optional;
import java.util.Set;
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

    private final Supplier<? extends StackStepInfo<R>> supplier;

    ResultOrError(Supplier<? extends StackStepInfo<R>> supplier) {
        this.supplier = supplier;
    }

    static StackWalker.StackFrame getStackStep() {
        return StackWalker.getInstance()
            .walk(stream -> stream.filter(s ->
                    !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName())
                        && !s.toStackTraceElement().getClassName().equals(WithCloseable.class.getName())
                )
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

    @SuppressWarnings("unchecked")
    public ResultOrError<Optional<R>> continueWithOptional() {
        final var frame = getStackStep();
        return new ResultOrError<>(
            () -> {
                final var previousStackInfo = supplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfo<Optional<R>>) previousStackInfo;
                } else {
                    return new StackStepInfo<>(frame, previousStackInfo, lastOutput, Optional.ofNullable(lastOutput), null, null);
                }
            }
        );
    }

    public R get() {
        return getResult().get();
    }

    public Optional<R> getOption() {
        return Optional.ofNullable(get());
    }

    public <E extends RuntimeException> R getOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        return getResult().getOrSpecError(target, exceptionMapper);
    }

    public <E extends RuntimeException> R getOrSpecErrorBy(Class<E> target, Function<Result<R, Throwable>, E> exceptionMapper) {
        return getResult().getOrSpecErrorBy(target, exceptionMapper);
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        return getResult().getOptionOrSpecError(target, exceptionMapper);
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecErrorBy(Class<E> target, Function<Result<R, Throwable>, E> exceptionMapper) {
        return getResult().getOptionOrSpecErrorBy(target, exceptionMapper);
    }

    public Result<R, Throwable> getResult() {

        val prev = StackWalker.getInstance()
            .walk(stream -> {
                    val iter = stream.iterator();
                    if (iter.hasNext()) {
                        iter.next();
                        if (iter.hasNext()) {
                            return iter.next();
                        }
                        return null;
                    }
                    return null;
                }
            );

        val isSelfCall = (prev.toStackTraceElement().getClassName().equals(ResultOrError.class.getName())
            && !Set.of("get", "getOption", "getOrSpecError", "getOrSpecErrorBy", "getOptionOrSpecError", "getOptionOrSpecErrorBy")
            .contains(prev.toStackTraceElement().getMethodName())
        )
            || (
            prev.toStackTraceElement().getClassName().equals(WithCloseable.class.getName())
                && !Set.of("closeAndGetResult", "closeAndGet")
                .contains(prev.toStackTraceElement().getMethodName())
        );

        val rapped = isSelfCall ? supplier : (Supplier<? extends StackStepInfo<R>>) () -> {
            final var res = supplier.get();
            return new StackStepInfo<>(getStackStep(), res, res.output(), res.output(), res.throwable());
        };

        final var res = (rapped.get());
        if (res.isError()) {
            return Result.failure(res.throwable(), res);
        }
        return Result.success(res.output(), res);
    }

    public <W extends RuntimeException>
    Result<R, W> getResult(Class<W> target, Function<Throwable, W> exceptionMapper) {
        return getResult().mapError(target, exceptionMapper);
    }

}
