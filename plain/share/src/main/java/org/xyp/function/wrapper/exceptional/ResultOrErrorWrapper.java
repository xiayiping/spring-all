package org.xyp.function.wrapper.exceptional;

import org.xyp.function.ExceptionalFunction;
import org.xyp.function.ExceptionalRunnable;
import org.xyp.function.ExceptionalSupplier;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ResultOrErrorWrapper<T, E extends Exception> {

    static <T1, E1 extends Exception> ResultOrErrorWrapper<T1, E1> of(
        T1 t1,
        ExceptionWrapper<E1> wrapper
    ) {
        return new FunctionSuccessSpecErr<>(t1, wrapper);
    }

    static <T1, E1 extends Exception> ResultOrErrorWrapper<T1, E1> ofErr(
        Exception exception,
        ExceptionWrapper<E1> wrapper
    ) {
        return new FunctionErrorSpecErr<>(exception, wrapper);
    }

    static <R, E1 extends Exception> ResultOrErrorWrapper<R, E1> on(
        ExceptionalSupplier<R> supplier,
        ExceptionWrapper<E1> wrapper
    ) {
        try {
            return ResultOrErrorWrapper.of(supplier.get(), wrapper);
        } catch (Exception e) {
            return ResultOrErrorWrapper.ofErr(e, wrapper);
        }
    }

    static <R, E1 extends Exception> ResultOrErrorWrapper<R, E1> onOpt(
        ExceptionalSupplier<Optional<R>> supplier,
        ExceptionWrapper<E1> wrapper
    ) {
        try {
            return new FunctionSuccessSpecErr<>(supplier.get().orElse(null), wrapper);
        } catch (Exception e) {
            return ResultOrErrorWrapper.ofErr(e, wrapper);
        }
    }

    static <E1 extends Exception> ConsumeResultErrWrapper<E1> doWith(
        ExceptionalRunnable runnable,
        ExceptionWrapper<E1> wrapper
    ) {
        try {
            runnable.run();
            return new ConsumeSuccessSpecErrWrapper<>(wrapper);
        } catch (Exception e) {
            return new ConsumeErrorWrapper<>(e, wrapper);
        }
    }

    boolean isError();

    ResultOrErrorWrapper<T, E> ifPresent(Consumer<? super T> action);

    ResultOrErrorWrapper<T, E> ifError(Consumer<? super E> errAction);

    ResultOrErrorWrapper<T, E> ifEmpty(Runnable emptyAction);

    ResultOrErrorWrapper<T, E> filter(Predicate<? super T> predicate);

    <U> ResultOrErrorWrapper<U, E> map(ExceptionalFunction<? super T, ? extends U> mapper);

    <U> ResultOrErrorWrapper<U, E> flatMap(ExceptionalFunction<? super T, ? extends ResultOrErrorWrapper<? extends U, E>> mapper);

    <U> ResultOrErrorWrapper<U, E> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper);

    Stream<T> stream();

    boolean isPresent();

    T get() throws E;

    T get(Function<Exception, ? extends T> exceptionMapper);

    Optional<T> getOptional() throws E;

    Optional<T> getOptional(Function<Exception, T> exceptionMapper);

    E getException();
}
