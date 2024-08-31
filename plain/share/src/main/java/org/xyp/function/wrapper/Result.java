package org.xyp.function.wrapper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Result<T, E extends Throwable> {
    boolean isSuccess();

    static <T, E extends Throwable> Result<T, E> success(T t, StackStepInfo<T> stackStepInfo) {
        return new Success<>(t, stackStepInfo);
    }

    static <T, E extends Throwable> Result<T, E> failure(E throwable, StackStepInfo<T> stackStepInfo) {
        return new Failure<>(throwable, stackStepInfo);
    }

    T get();

    E getError();

    <R extends RuntimeException>
    T getOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper);

    Optional<T> getOption();

    Optional<T> getOptionEvenErr(Consumer<E> exceptionConsumer);

    <R extends RuntimeException> Optional<T>
    getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper);

    void ifError(Consumer<E> consumer);

    T getOrFallBackForError(Function<E, T> exceptionMapper);

    Optional<StackStepInfo<T>> getStackStepInfo();
}
