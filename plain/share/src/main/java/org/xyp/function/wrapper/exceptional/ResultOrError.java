package org.xyp.function.wrapper.exceptional;

import org.xyp.function.ExceptionalFunction;
import org.xyp.function.ExceptionalRunnable;
import org.xyp.function.ExceptionalSupplier;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ResultOrError<T> {

    static <T1> ResultOrError<T1> of(T1 t1) {
        return new FunctionSuccess<>(t1);
    }

    static <T1, E1 extends Exception> ResultOrError<T1> ofErr(E1 exception) {
        return new FunctionError<>(exception);
    }

    static <R> ResultOrError<R> on(ExceptionalSupplier<R> supplier) {
        try {
            return ResultOrError.of(supplier.get());
        } catch (Exception e) {
            return ResultOrError.ofErr(e);
        }
    }

    static <R> ResultOrError<R> onOpt(ExceptionalSupplier<Optional<R>> supplier) {
        try {
            return new FunctionSuccess<>(supplier.get().orElse(null));
        } catch (Exception e) {
            return ResultOrError.ofErr(e);
        }
    }

    static ConsumeResult doWith(ExceptionalRunnable runnable) {
        try {
            runnable.run();
            return new ConsumeSuccess();
        } catch (Exception e) {
            return new ConsumeError(e);
        }
    }

    boolean isError();

    boolean isPresent();

    ResultOrError<T> filter(Predicate<? super T> predicate);

    <U> ResultOrError<U> map(ExceptionalFunction<? super T, ? extends U> mapper);

    <U> ResultOrError<U> flatMap(ExceptionalFunction<? super T, ? extends ResultOrError<? extends U>> mapper);

    <U> ResultOrError<U> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper);

    Stream<T> stream();

    ResultOrError<T> ifPresent(Consumer<? super T> action);

    ResultOrError<T> ifError(Consumer<? super Exception> errAction);

    ResultOrError<T> ifEmpty(Runnable emptyAction);

    T get();

    T getOrElseGet(Function<Exception, ? extends T> exceptionMapper);

    T getOrThrow(Function<Exception, ? extends RuntimeException> exceptionMapper);

    T getOrThrow() throws Exception;

    Optional<T> getOptionalOrThrow(Function<Exception, RuntimeException> exceptionMapper);

    Optional<T> getOptional();

    Optional<T> getOptionalOrThrow() throws Exception;

    Optional<T> getOptional(Function<Exception, T> exceptionMapper);

    <E extends Exception> ResultOrErrorWrapper<T, E> specError(
        ExceptionWrapper<E> exceptionMapper
    );
}
