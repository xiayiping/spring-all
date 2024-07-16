package org.xyp.function.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.xyp.function.Fun;
import org.xyp.function.FunctionException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public record Failure<T, E extends Throwable>(
    E throwable
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
    public <RTE extends RuntimeException> T getOrSpecError(Class<RTE> rteClass, Function<E, RTE> exceptionMapper) {
        if (rteClass.isAssignableFrom(throwable.getClass())) {
            throw rteClass.cast(throwable);
        }
        throw exceptionMapper.apply(throwable);
    }

    @Override
    public Optional<T> getOption() {
        throw Fun.convertRte(throwable, RuntimeException.class, FunctionException::new);
    }

    @Override
    public Optional<T> getOptionEvenErr() {
        log.warn("return empty optional for error {}", throwable.getClass().getName(), throwable);
        return Optional.empty();
    }

    @Override
    public <RTE extends RuntimeException> Optional<T> getOptionOrSpecError(Class<RTE> rteClass, Function<E, RTE> exceptionMapper) {
        if (rteClass.isAssignableFrom(throwable.getClass())) {
            throw rteClass.cast(throwable);
        }
        throw exceptionMapper.apply(throwable);
    }

    @Override
    public void ifError(Consumer<E> consumer) {
        consumer.accept(throwable);
    }
}
