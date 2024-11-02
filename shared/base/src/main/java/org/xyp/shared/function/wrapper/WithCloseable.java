package org.xyp.shared.function.wrapper;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.xyp.shared.function.*;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

@Slf4j
public class WithCloseable<C extends AutoCloseable, T> {

    static StackWalker.StackFrame getStackStep() {
        return StackWalker.getInstance()
            .walk(stream -> stream.filter(s ->
                    !s.toStackTraceElement().getClassName().equals(WithCloseable.class.getName())
                        && !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName())
                )
                .findFirst())
            .orElse(null);
    }

    private static <L extends AutoCloseable> StackStepInfoWithCloseable<L, L>
    openStackStepInfoWithCloseable(ExceptionalSupplier<L> open, StackWalker.StackFrame frame) {
        try {
            final var closeable = open.get();
            return new StackStepInfoWithCloseable<>(frame, null, closeable, null, closeable, null);
        } catch (Throwable t) {
            return new StackStepInfoWithCloseable<>(frame, null, null, null, null, t);
        }
    }

    @SuppressWarnings("unchecked")
    static <R, U, C extends AutoCloseable> StackStepInfoWithCloseable<C, U>
    getStackStepInfoByMapper(
        ExceptionalFunction<? super R, ? extends U> mapper,
        StackStepInfoWithCloseable<C, R> previousStackInfo,
        StackWalker.StackFrame frame
    ) {
        final var lastOutput = previousStackInfo.output();
        if (previousStackInfo.isError()) {
            return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
        } else if (null != lastOutput) {
            final var closeable = previousStackInfo.closeable();
            try {
                final var mappedVal = mapper.apply(lastOutput);
                return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, mappedVal, null);
            } catch (Throwable t) {
                return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
            }
        } else {
            return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
        }
    }

    static <R, C extends AutoCloseable> StackStepInfoWithCloseable<C, R>
    getStackByConsume(
        ExceptionalConsumer<? super R> consumer,
        StackStepInfoWithCloseable<C, R> prevStack,
        StackWalker.StackFrame frame
    ) {
        final var lastOutput = prevStack.output();
        if (prevStack.isError()) {
            return prevStack;
        } else if (null != lastOutput) {
            final var closeable = prevStack.closeable();
            try {
                consumer.accept(lastOutput);
                return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, lastOutput, lastOutput, null);
            } catch (Throwable throwable) {
                return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, lastOutput, null, throwable);
            }
        } else {
            return prevStack;
        }
    }

    static <C extends AutoCloseable, R> StackStepInfoWithCloseable<C, R> getStackStepInfoByFilter(
        Predicate<? super R> predicate,
        StackStepInfoWithCloseable<C, R> prevStack,
        StackWalker.StackFrame frame
    ) {
        if (prevStack.isError()) {
            return prevStack;
        }
        final var lastOutput = prevStack.output();
        final var closeable = prevStack.closeable();
        try {
            if (null != lastOutput && predicate.test(lastOutput)) {
                return prevStack;
            } else {
                return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, null, null, null);
            }
        } catch (Throwable throwable) {
            return new StackStepInfoWithCloseable<>(frame, prevStack, closeable, lastOutput, null, throwable);
        }
    }

    public static <L extends AutoCloseable> WithCloseable<L, L> open(ExceptionalSupplier<L> open) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> openStackStepInfoWithCloseable(open, frame),
            // (closeable, throwable) onException
            (__, ___) -> {
            }
        );
    }

    public static <L extends AutoCloseable> WithCloseable<L, L> open(
        ExceptionalSupplier<L> open,
        BiConsumer<L, Throwable> exceptionConsumer
    ) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> openStackStepInfoWithCloseable(open, frame),
            exceptionConsumer
        );
    }

    final Supplier<StackStepInfoWithCloseable<C, T>> closeableSupplier;
    final BiConsumer<C, Throwable> exceptionConsumer;

    private WithCloseable(
        Supplier<StackStepInfoWithCloseable<C, T>> closeableSupplier,
        BiConsumer<C, Throwable> exceptionConsumer
    ) {
        this.closeableSupplier = closeableSupplier;
        this.exceptionConsumer = exceptionConsumer;
    }

    public <U> WithCloseable<C, U> map(ExceptionalFunction<? super T, ? extends U> function) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                return getStackStepInfoByMapper(function, previousStackInfo, frame);
            },
            exceptionConsumer
        );
    }

    public WithCloseable<C, T> fallBackEmpty(Function<C, T> emptySupplier) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return previousStackInfo;
                } else if (null == lastOutput) {
                    final var closeable = previousStackInfo.closeable();
                    try {
                        final var mappedVal = emptySupplier.apply(closeable);
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, null, mappedVal, null);
                    } catch (Throwable t) {
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, null, null, t);
                    }
                } else {
                    return previousStackInfo;
                }
            },
            exceptionConsumer
        );
    }

    public WithCloseable<C, T> consume(ExceptionalConsumer<? super T> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                return getStackByConsume(consumer, previousStackInfo, frame);
            },
            this.exceptionConsumer
        );
    }

    public WithCloseable<C, T> doOnError(ExceptionalBiConsumer<C, Throwable> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                try {
                    if (previousStackInfo.isError()) {
                        consumer.accept(previousStackInfo.closeable(), previousStackInfo.throwable());
                        final var closeable = previousStackInfo.closeable();
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, previousStackInfo.throwable());
                    } else {
                        return previousStackInfo;
                    }
                } catch (Throwable t) {
                    final var closeable = previousStackInfo.closeable();
                    return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
                }
            },
            this.exceptionConsumer
        );
    }

    public WithCloseable<C, T> mapOnError(ExceptionalBiFunction<C, Throwable, T> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                try {
                    if (previousStackInfo.isError()) {
                        val newValueForError = consumer.apply(previousStackInfo.closeable(), previousStackInfo.throwable());
                        final var closeable = previousStackInfo.closeable();
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, previousStackInfo.throwable(), newValueForError, null);
                    } else {
                        return previousStackInfo;
                    }
                } catch (Throwable t) {
                    final var closeable = previousStackInfo.closeable();
                    return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
                }
            },
            this.exceptionConsumer
        );
    }


    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> mapWithCloseable(ExceptionalBiFunction<? super C, ? super T, ? extends U> biFunction) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
                } else {
                    final var closeable = previousStackInfo.closeable();
                    try {
                        final var mappedVal = biFunction.apply(closeable, lastOutput);
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, mappedVal, null);
                    } catch (Throwable t) {
                        return new StackStepInfoWithCloseable<>(frame, previousStackInfo, closeable, lastOutput, null, t);
                    }
                }
            },
            this.exceptionConsumer
        );
    }

    public WithCloseable<C, T> filter(Predicate<? super T> predicate) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var prevStack = closeableSupplier.get();
                return getStackStepInfoByFilter(predicate, prevStack, frame);
            },
            this.exceptionConsumer
        );
    }

    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> flatMap(Function<? super T, ResultOrError<U>> mapper) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
                } else if (null != lastOutput) {
                    val mapperROE = mapper.apply(lastOutput);
                    final var mappedResult = mapperROE.getResultInPackage(mapperROE.supplier());
                    final var childStack = mappedResult.getStackStepInfo();
                    final var closeable = previousStackInfo.closeable();
                    if (mappedResult.isSuccess()) {
                        final var mappedVal = mappedResult.get();
                        return new StackStepInfoWithCloseable<C, U>(frame, previousStackInfo, closeable, lastOutput, mappedVal, null, childStack.orElse(null));
                    } else {
                        return new StackStepInfoWithCloseable<C, U>(frame, previousStackInfo, closeable, lastOutput, null, mappedResult.getError(), childStack.orElse(null));
                    }
                } else {
                    return (StackStepInfoWithCloseable<C, U>) previousStackInfo;
                }
            },
            this.exceptionConsumer
        );
    }

    @SuppressWarnings("unchecked")
    public WithCloseable<C, Optional<T>> continueWithOptional() {
        final var frame = getStackStep();
        return new WithCloseable<C, Optional<T>>(
            () -> {
                final var previousStackInfo = closeableSupplier.get();
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfoWithCloseable<C, Optional<T>>) previousStackInfo;
                } else {
                    return new StackStepInfoWithCloseable<>(frame, previousStackInfo, previousStackInfo.closeable(), lastOutput, Optional.ofNullable(lastOutput), null, null);
                }
            },
            this.exceptionConsumer
        );
    }

    public T closeAndGet() {
        return closeAndGetResult().get();
    }

    public <E extends RuntimeException> T closeAndGet(
        Class<E> target,
        Function<Throwable, E> exceptionMapper
    ) {
        return closeAndGetResult().getOrSpecError(target, exceptionMapper);
    }

    public Result<T, Throwable> closeAndGetResult() {
        return convertToResult().getResult();
    }


    public <W extends RuntimeException>
    Result<T, W> closeAndGetResult(Class<W> target, Function<Throwable, W> exceptionMapper) {
        return closeAndGetResult().mapError(target, exceptionMapper);
    }

    public ResultOrError<T> convertToResult() {

        final var frame = getStackStep();
        val rapped = (Supplier<? extends StackStepInfo<T>>) () -> {
            C localCloseable = null;
            StackStepInfoWithCloseable<C, T> localRes = null;
            try (var res = closeableSupplier.get()) {
                localCloseable = res.closeable();
                localRes = res;
                if (res.isError()) {
                    this.exceptionConsumer.accept(localCloseable, res.throwable());
                    localRes = new StackStepInfoWithCloseable<>(frame, res, localCloseable, res.output(), res.output(), res.throwable());
                }
            } catch (Throwable e) {
                localRes = new StackStepInfoWithCloseable<>(
                    StackWalker.getInstance().walk(Stream::findFirst).orElse(localRes.stackFrame()),
                    new StackStepInfoWithCloseable<>(
                        frame,
                        localRes,
                        localCloseable,
                        localRes.output(),
                        localRes.output(),
                        localRes.throwable()
                    ),
                    localCloseable,
                    localRes.output(),
                    localRes.output(),
                    e
                );
            }
            return localRes;
        };

        return new ResultOrError<>(rapped);
    }
}
