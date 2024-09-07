package org.xyp.function.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Result<T, E extends Throwable> {

    Logger log = LoggerFactory.getLogger(Result.class);

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

    <R extends RuntimeException> T getOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper);

    Optional<T> getOption();

    Optional<T> getOptionEvenErr(Function<E, T> exceptionFallBack);

    <R extends RuntimeException> Optional<T>
    getOptionOrSpecError(Class<R> rteClass, Function<E, R> exceptionMapper);

    <R extends RuntimeException> Optional<T>
    getOptionOrSpecErrorBy(Class<R> rteClass, Function<Result<T, E>, R> exceptionMapper);

    Result<T, E> ifError(Consumer<E> consumer);

    T getOrFallBackForError(Function<E, T> exceptionMapper);

    Optional<StackStepInfo<T>> getStackStepInfo();

    default Result<T, E> doIf(Predicate<Result<T, E>> me, Consumer<Result<T, E>> consumer) {
        if (me.test(this)) {
            consumer.accept(this);
        }
        return this;
    }

    default Result<T, E> doIfError(Consumer<Result<T, E>> consumer) {
        return doIf(Failure.class::isInstance, consumer);
    }

    <W extends RuntimeException>
    Result<T, W> mapError(Class<W> target, Function<E, W> exceptionMapper);


    default Result<T, E> traceDebugOrError(
        Supplier<Boolean> needDebug, Consumer<String> debugLogger,
        Supplier<Boolean> needError, Consumer<String> errLogger
    ) {
        try {
            if (isSuccess() && needDebug.get()) {
                debugLogger.accept("check debug");
                StackLogUtil.logTrace(debugLogger, getStackStepInfo().orElse(null));
            } else if (needError.get()) {
                errLogger.accept("check error");
                StackLogUtil.logTrace(errLogger, getStackStepInfo().orElse(null));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return this;
    }
}