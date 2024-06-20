package org.xyp.function.wrapper.exceptional;

import lombok.val;
import org.xyp.function.ExceptionalFunction;
import org.xyp.function.FunctionException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FunctionError<T, E extends Exception> implements ResultOrError<T> {

    private final E error;

    public FunctionError(E error) {
        this.error = error;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public ResultOrError<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this;
    }

    @Override
    public <U> ResultOrError<U> map(ExceptionalFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        @SuppressWarnings("unchecked")
        val r = (ResultOrError<U>) this;
        return r;
    }

    @Override
    public <U> ResultOrError<U> flatMap(ExceptionalFunction<? super T, ? extends ResultOrError<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        @SuppressWarnings("unchecked")
        val r = (ResultOrError<U>) this;
        return r;
    }

    @Override
    public <U> ResultOrError<U> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        @SuppressWarnings("unchecked")
        val r = (ResultOrError<U>) this;
        return r;
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public ResultOrError<T> ifPresent(Consumer<? super T> action) {
        return this;
    }

    @Override
    public ResultOrError<T> ifError(Consumer<? super Exception> errAction) {
        errAction.accept(error);
        return this;
    }

    @Override
    public ResultOrError<T> ifEmpty(Runnable emptyAction) {
        return this;
    }

    @Override
    public T get() {
        if (error instanceof RuntimeException rte) {
            throw rte;
        }
        throw new FunctionException(error);
    }

    public T getOrThrow() throws Exception {
        throw error;
    }

    public Optional<T> getOptionalOrThrow() throws Exception {
        throw error;
    }

    @Override
    public T getOrElseGet(Function<Exception, ? extends T> exceptionMapper) {
        return exceptionMapper.apply(error);
    }

    @Override
    public T getOrThrow(Function<Exception, ? extends RuntimeException> exceptionMapper
    ) {
        throw exceptionMapper.apply(error);
    }

    @Override
    public Optional<T> getOptionalOrThrow(Function<Exception, RuntimeException> exceptionMapper
    ) {
        throw exceptionMapper.apply(error);
    }

    @Override
    public Optional<T> getOptional() {
        return Optional.empty();
    }

    @Override
    public Optional<T> getOptional(Function<Exception, T> exceptionMapper) {
        return Optional.ofNullable(exceptionMapper.apply(error));
    }

    @Override
    public <E1 extends Exception> ResultOrErrorWrapper<T, E1> specError(
        ExceptionWrapper<E1> exceptionMapper
    ) {
        if (exceptionMapper.exceptionClass().isAssignableFrom(error.getClass())) {
            return ResultOrErrorWrapper.ofErr(this.error, new ExceptionWrapper<>(
                exceptionMapper.exceptionClass(),
                exceptionMapper.exceptionClass()::cast)
            );
        }
        return ResultOrErrorWrapper.ofErr(this.error, exceptionMapper);
    }

    @Override
    public <E1 extends Exception> ResultOrErrorWrapper<T, E1> specError(
        Class<E1> exceptionClass,
        Function<Exception, E1> wrapper
    ) {
        if (exceptionClass.isAssignableFrom(error.getClass())) {
            return ResultOrErrorWrapper.ofErr(this.error, new ExceptionWrapper<>(exceptionClass, exceptionClass::cast));
        }
        return ResultOrErrorWrapper.ofErr(this.error, new ExceptionWrapper<>(exceptionClass, wrapper));
    }
}
