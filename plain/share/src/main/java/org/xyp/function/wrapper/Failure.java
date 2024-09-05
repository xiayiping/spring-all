package org.xyp.function.wrapper;

import org.xyp.function.Fun;
import org.xyp.function.FunctionException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Failure<T, E extends Throwable>(
    E throwable,
    StackStepInfo<T> stackStepInfo
) implements Result<T, E> {
    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public T get() {
        throw Fun.convertRte(throwable, RuntimeException.class, FunctionException::new);
    }

    @Override
    public E getError() {
        return throwable;
    }

    @Override
    public <R extends RuntimeException> T getOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(throwable.getClass())) {
            throw rteClass.cast(throwable);
        }
        throw exceptionMapper.apply(throwable);
    }

    @Override
    public <R extends RuntimeException> T getOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(throwable.getClass())) {
            throw rteClass.cast(throwable);
        }
        throw exceptionMapper.apply(this);
    }

    @Override
    public Optional<T> getOption() {
        throw Fun.convertRte(throwable, RuntimeException.class, FunctionException::new);
    }

    @Override
    public Optional<T> getOptionEvenErr(Function<E, T> exceptionFallBack) {
        return Optional.ofNullable(exceptionFallBack.apply(throwable));
    }

    @Override
    public <R extends RuntimeException> Optional<T> getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(throwable.getClass())) {
            throw rteClass.cast(throwable);
        }
        throw exceptionMapper.apply(throwable);
    }

    @Override
    public <R extends RuntimeException> Optional<T>
    getOptionOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper) {
        if (rteClass.isAssignableFrom(throwable.getClass())) {
            throw rteClass.cast(throwable);
        }
        throw exceptionMapper.apply(this);
    }

    @Override
    public Result<T, E> ifError(Consumer<E> consumer) {
        consumer.accept(throwable);
        return this;
    }

    @Override
    public T getOrFallBackForError(Function<E, T> exceptionMapper) {
        return exceptionMapper.apply(throwable);
    }

    @Override
    public Optional<StackStepInfo<T>> getStackStepInfo() {
        return Optional.ofNullable(stackStepInfo);
    }
}
