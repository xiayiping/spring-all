package org.xyp.function.wrapper;

import org.xyp.function.*;

import java.util.Optional;
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
public final class ResultOrError<R> {

    private final ExceptionalSupplier<R> supplier;
    private final StackWalker.StackFrame stackStep;

    private ResultOrError(ExceptionalSupplier<R> supplier, StackWalker.StackFrame stackStep) {
        this.supplier = supplier;
        this.stackStep = stackStep;
    }

    private static StackWalker.StackFrame getStackStep() {
        final var stack = StackWalker.getInstance()
            .walk(stream -> stream.filter(s -> !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName()))
                .findFirst())
            .orElse(null);
        System.out.println(stack);
        return stack;
    }

    public static <T1> ResultOrError<T1> of(T1 t1) {
        return new ResultOrError<>(() -> t1, getStackStep());
    }

    public static <R> ResultOrError<R> on(ExceptionalSupplier<R> supplier) {
        return new ResultOrError<>(supplier, getStackStep());
    }

    public ResultOrError<R> filter(Predicate<? super R> predicate) {
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                if (null != innerResult && predicate.test(innerResult)) {
                    return innerResult;
                }
                return null;
            }, getStackStep()
        );
    }

    public ResultOrError<R> fallbackForEmpty(Supplier<R> emptySupplier) {
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                if (null != innerResult) {
                    return innerResult;
                }
                return emptySupplier.get();
            }, getStackStep()
        );
    }

    public ResultOrError<R> consume(ExceptionalConsumer<? super R> consumer) {
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                if (null != innerResult) {
                    consumer.accept(innerResult);
                }
                return innerResult;
            }, getStackStep()
        );
    }

    public <U> ResultOrError<U> map(ExceptionalFunction<? super R, ? extends U> mapper) {

        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                if (null != innerResult) {
                    return mapper.apply(innerResult);
                }
                return null;
            }, getStackStep()
        );
    }

    public <U> ResultOrError<U> flatMap(ExceptionalFunction<? super R, ResultOrError<U>> mapper) {
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                if (null != innerResult) {
                    final ResultOrError<U> optional = mapper.apply(innerResult);
                    return optional.get();
                }
                return null;
            }, getStackStep()
        );
    }

    public R get() {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw Fun.convertRte(e, RuntimeException.class, FunctionException::new);
        }
    }

    public Optional<R> getOption() {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Throwable e) {
            throw Fun.convertRte(e, RuntimeException.class, FunctionException::new);
        }
    }

    public <E extends RuntimeException> R getOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw Fun.convertRte(e, target, exceptionMapper);
        }
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Throwable e) {
            throw Fun.convertRte(e, target, exceptionMapper);
        }
    }

    public Result<R, Throwable> getResult() {
        try {
            return Result.success(supplier.get());
        } catch (Throwable e) {
            return Result.failure(e);
        }
    }

    public <E extends RuntimeException> Result<R, E> getResultOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        try {
            return Result.success(supplier.get());
        } catch (Throwable e) {
            return Result.failure(Fun.convertRte(e, target, exceptionMapper));
        }
    }
}
