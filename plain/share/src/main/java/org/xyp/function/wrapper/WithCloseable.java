package org.xyp.function.wrapper;

import org.xyp.function.*;

import java.util.function.*;

import static org.xyp.function.wrapper.ResultOrError.*;

public class WithCloseable<C extends AutoCloseable, T> {

    static StackWalker.StackFrame getStackStep() {
        return StackWalker.getInstance()
            .walk(stream -> stream.filter(s -> !s.toStackTraceElement().getClassName().equals(WithCloseable.class.getName()))
                .findFirst())
            .orElse(null);
    }

    public static <L extends AutoCloseable> WithCloseable<L, L> open(ExceptionalSupplier<L> open) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            open,
            c -> new StackStepInfo<>(frame, null, null, c, null),
            (c, t) -> {
            },
            c -> {
            }
        );
    }

    public static <L extends AutoCloseable> WithCloseable<L, L> open(
        ExceptionalSupplier<L> open,
        BiConsumer<L, Throwable> exceptionConsumer,
        Consumer<L> finallyConsumer
    ) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            open,
            c -> new StackStepInfo<>(frame, null, null, c, null),
            exceptionConsumer,
            finallyConsumer
        );
    }

    final ExceptionalSupplier<C> closeableSupplier;
    final BiConsumer<C, Throwable> exceptionConsumer;
    final Consumer<C> finallyConsumer;
    final Function<? super C, StackStepInfo<T>> innerResultMapper;

    private WithCloseable(
        ExceptionalSupplier<C> closeableSupplier,
        Function<? super C, StackStepInfo<T>> mapper,
        BiConsumer<C, Throwable> exceptionConsumer,
        Consumer<C> finallyConsumer
    ) {
        this.closeableSupplier = closeableSupplier;
        this.innerResultMapper = mapper;
        this.exceptionConsumer = exceptionConsumer;
        this.finallyConsumer = finallyConsumer;
    }

    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> map(ExceptionalFunction<? super T, ? extends U> function) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var previousStackInfo = innerResultMapper.apply(closeable);
                return getStackStepInfoByMapper(function, previousStackInfo, frame);
            },
            exceptionConsumer,
            finallyConsumer
        );
    }

    public WithCloseable<C, T> fallBackEmpty(Function<C, T> emptySupplier) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var previousStackInfo = innerResultMapper.apply(closeable);
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return previousStackInfo;
                } else if (null == lastOutput) {
                    try {
                        final var mappedVal = emptySupplier.apply(closeable);
                        return new StackStepInfo<>(frame, previousStackInfo, null, mappedVal, null);
                    } catch (Throwable t) {
                        return new StackStepInfo<>(frame, previousStackInfo, null, null, t);
                    }
                } else {
                    return previousStackInfo;
                }
            },
            exceptionConsumer,
            finallyConsumer
        );
    }

    public WithCloseable<C, T> consume(ExceptionalConsumer<? super T> consumer) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var previousStackInfo = innerResultMapper.apply(closeable);
                return getStackByConsume(consumer, previousStackInfo, frame);
            },
            this.exceptionConsumer,
            finallyConsumer
        );
    }

    public WithCloseable<C, T> logTrace(Consumer<String> logger) {
        return logTrace(logger, true);
    }

    public WithCloseable<C, T> logTrace(Consumer<String> logger, Supplier<Boolean> shouldLog) {
        return logTrace(logger, shouldLog.get());
    }

    public WithCloseable<C, T> logTrace(Consumer<String> logger, boolean shouldLog) {
        if(!shouldLog) {
            return this;
        }
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var previousStackInfo = innerResultMapper.apply(closeable);
                final var lastOutput = previousStackInfo.output();
                try {
                    final var current = new StackStepInfo<T>(frame, previousStackInfo, lastOutput, lastOutput, previousStackInfo.throwable());
                    logTraceInternal(logger, current);
                    return current;
                } catch (Throwable t) {
                    return new StackStepInfo<>(frame, previousStackInfo, previousStackInfo, null, t);
                }
            },
            this.exceptionConsumer,
            finallyConsumer
        );
    }

    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> mapWithCloseable(ExceptionalBiFunction<? super C, ? super T, ? extends U> biFunction) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var previousStackInfo = innerResultMapper.apply(closeable);
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfo<U>) previousStackInfo;
                } else {
                    try {
                        final var mappedVal = biFunction.apply(closeable, lastOutput);
                        return new StackStepInfo<>(frame, previousStackInfo, lastOutput, mappedVal, null);
                    } catch (Throwable t) {
                        return new StackStepInfo<>(frame, previousStackInfo, lastOutput, null, t);
                    }
                }
            },
            this.exceptionConsumer,
            finallyConsumer
        );
    }

    public WithCloseable<C, T> filter(Predicate<? super T> predicate) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var prevStack = innerResultMapper.apply(closeable);
                return getStackStepInfoByFilter(predicate, prevStack, frame);
            },
            this.exceptionConsumer,
            finallyConsumer
        );
    }

    @SuppressWarnings("unchecked")
    public <U> WithCloseable<C, U> flatMap(Function<? super T, ResultOrError<U>> mapper) {
        final var frame = getStackStep();
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final var previousStackInfo = innerResultMapper.apply(closeable);
                final var lastOutput = previousStackInfo.output();
                if (previousStackInfo.isError()) {
                    return (StackStepInfo<U>) previousStackInfo;
                } else if (null != lastOutput) {
                    final var mappedResult = mapper.apply(lastOutput).getResult();
                    final var childStack = mappedResult.getStackStepInfo();
                    if(mappedResult.isSuccess()) {
                        final var mappedVal = mappedResult.get();
                        return new StackStepInfo<U>(frame, previousStackInfo, lastOutput, mappedVal, null, childStack.orElse(null));
                    } else {
                        return new StackStepInfo<U>(frame, previousStackInfo, lastOutput, null, mappedResult.getError(), childStack.orElse(null));
                    }
                } else {
                    return (StackStepInfo<U>) previousStackInfo;
                }
            },
            this.exceptionConsumer,
            finallyConsumer
        );
    }

    public T closeAndGet() {
        C localCloseable = null;
        try (var closeable = closeableSupplier.get()) {
            localCloseable = closeable;
            final var res = innerResultMapper.apply(closeable);
            if (res.isError()) {
                throw res.throwable();
            }
            return res.output();
        } catch (Throwable e) {
            throw Fun.convertRte(e, RuntimeException.class, FunctionException::new);
        } finally {
            if (null != localCloseable)
                finallyConsumer.accept(localCloseable);
        }
    }

    public <E extends RuntimeException> T closeAndGet(
        Class<E> target,
        Function<Throwable, E> exceptionMapper
    ) {
        C localCloseable = null;
        try (var closeable = closeableSupplier.get()) {
            localCloseable = closeable;
            final var res = innerResultMapper.apply(closeable);
            if (res.isError()) {
                throw res.throwable();
            }
            return res.output();
        } catch (Throwable e) {
            exceptionConsumer.accept(localCloseable, e);
            throw Fun.convertRte(e, target, exceptionMapper);
        } finally {
            if (null != localCloseable) {
                finallyConsumer.accept(localCloseable);
            }
        }
    }

    public Result<T, Throwable> closeAndGetResult() {
        C localCloseable = null;
        try (var closeable = closeableSupplier.get()) {
            localCloseable = closeable;
            final var res = innerResultMapper.apply(closeable);
            if (res.isError()) {
                exceptionConsumer.accept(localCloseable, res.throwable());
                return Result.failure(res.throwable(), res);
            }
            return Result.success(res.output(), res);
        } catch (Throwable e) {
            exceptionConsumer.accept(localCloseable, e);
            return Result.failure(e, null);
        } finally {
            if (null != localCloseable)
                finallyConsumer.accept(localCloseable);
        }
    }

    public <E extends RuntimeException> Result<T, E> closeAndGetResult(
        Class<E> target,
        Function<Throwable, E> exceptionMapper
    ) {
        C localCloseable = null;
        try (var closeable = closeableSupplier.get()) {
            localCloseable = closeable;
            final var res = innerResultMapper.apply(closeable);
            if (res.isError()) {
                exceptionConsumer.accept(localCloseable, res.throwable());
                return Result.failure(Fun.convertRte(res.throwable(), target, exceptionMapper), res);
            }
            return Result.success(res.output(), res);
        } catch (Throwable e) {
            exceptionConsumer.accept(localCloseable, e);
            final var ex = Fun.convertRte(e, target, exceptionMapper);
            return Result.failure(ex, null);
        } finally {
            if (null != localCloseable)
                finallyConsumer.accept(localCloseable);
        }
    }
}
