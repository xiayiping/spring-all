package org.xyp.function.wrapper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Result<T, E extends Throwable> {
    boolean isSuccess();

    static <T, E extends Throwable> Result<T, E> success(T t, StackStepInfo stackStepInfo) {
        return new Success<>(t, stackStepInfo);
    }

    static <T, E extends Throwable> Result<T, E> failure(E throwable) {
        return new Failure<>(throwable);
    }

    T get();

    <RTE extends RuntimeException>
    T getOrSpecError(Class<RTE> rteClass, Function<E, RTE> exceptionMapper);

    Optional<T> getOption();

    Optional<T> getOptionEvenErr(Consumer<E> exceptionConsumer);

    <RTE extends RuntimeException> Optional<T>
    getOptionOrSpecError(Class<RTE> rteClass, Function<E, RTE> exceptionMapper);

    void ifError(Consumer<E> consumer);
}
