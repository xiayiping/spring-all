package org.xyp.function.wrapper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Success<T, E extends Throwable>(T value) implements Result<T, E> {

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public <RTE extends RuntimeException> T getOrSpecError(Class<RTE> rteClass, Function<E, RTE> exceptionMapper) {
        return value;
    }

    @Override
    public Optional<T> getOption() {
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<T> getOptionEvenErr() {
        return Optional.ofNullable(value);
    }

    @Override
    public <RTE extends RuntimeException> Optional<T> getOptionOrSpecError(Class<RTE> rteClass, Function<E, RTE> exceptionMapper) {
        return Optional.ofNullable(value);
    }

    @Override
    public void ifError(Consumer<E> consumer) {
        // no need
    }
}
