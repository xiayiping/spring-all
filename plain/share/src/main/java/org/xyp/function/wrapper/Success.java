package org.xyp.function.wrapper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Success<T, E extends Throwable>(T value, StackStepInfo<T> stackStepInfo) implements Result<T, E> {

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public E getError() {
        return null;
    }

    @Override
    public <R extends RuntimeException> T getOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        return value;
    }

    @Override
    public Optional<T> getOption() {
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<T> getOptionEvenErr(Consumer<E> exceptionConsumer) {
        return Optional.ofNullable(value);
    }

    @Override
    public <R extends RuntimeException> Optional<T> getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper) {
        return Optional.ofNullable(value);
    }

    @Override
    public void ifError(Consumer<E> consumer) {
        // no need
    }

    @Override
    public T getOrFallBackForError(Function<E, T> exceptionMapper) {
        return value;
    }

    @Override
    public Optional<StackStepInfo<T>> getStackStepInfo() {
        return Optional.ofNullable(stackStepInfo);
    }
}
