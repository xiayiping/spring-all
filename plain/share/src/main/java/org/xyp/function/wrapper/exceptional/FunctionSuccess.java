package org.xyp.function.wrapper.exceptional;

import org.xyp.function.ExceptionalFunction;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FunctionSuccess<T> implements ResultOrError<T> {

    private final T result;

    public FunctionSuccess(T result) {
        this.result = result;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isPresent() {
        return null != result;
    }

    @Override
    public ResultOrError<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        try {
            return predicate.test(result) ? this : new FunctionSuccess<>(null);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrError<T>) new FunctionError<>(e);
            return re;
        }
    }

    @Override
    public <U> ResultOrError<U> map(ExceptionalFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccess<>(null);
            }
            U u = mapper.apply(result);
            return new FunctionSuccess<>(u);
        } catch (Exception e) {
            return new FunctionError<>(e);
        }
    }

    @Override
    public <U> ResultOrError<U> flatMap(ExceptionalFunction<? super T, ? extends ResultOrError<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccess<>(null);
            }
            @SuppressWarnings("unchecked")
            val r = (ResultOrError<U>) mapper.apply(result);
            return r;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrError<U>) new FunctionError<>(e);
            return re;
        }
    }

    @Override
    public <U> ResultOrError<U> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccess<>(null);
            }
            val r = mapper.apply(result);
            return ResultOrError.onOpt(() -> r);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrError<U>) new FunctionError<>(e);
            return re;
        }
    }

    @Override
    public Stream<T> stream() {
        return Stream.ofNullable(result);
    }

    @Override
    public ResultOrError<T> ifPresent(Consumer<? super T> action) {
        try {
            if (null != result) {
                action.accept(result);
            }
            return this;
        } catch (Exception e) {
            return new FunctionError<>(e);
        }
    }

    @Override
    public ResultOrError<T> ifError(Consumer<? super Exception> errAction) {
        return this;
    }

    @Override
    public ResultOrError<T> ifEmpty(Runnable emptyAction) {
        try {
            if (null == result) {
                emptyAction.run();
            }
            return this;
        } catch (Exception e) {
            return new FunctionError<>(e);
        }
    }

    @Override
    public T get() {
        return result;
    }

    public T getOrThrow() throws Exception {
        return get();
    }

    public Optional<T> getOptionalOrThrow() {
        return Optional.ofNullable(result);
    }

    @Override
    public T getOrElseGet(Function<Exception, ? extends T> exceptionMapper) {
        return result;
    }

    @Override
    public T getOrThrow(Function<Exception, ? extends RuntimeException> exceptionMapper
    ) {
        return result;
    }

    @Override
    public Optional<T> getOptionalOrThrow(Function<Exception, RuntimeException> exceptionMapper
    ) {
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<T> getOptional() {
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<T> getOptional(Function<Exception, T> ignored) {
        return Optional.ofNullable(result);
    }

    @Override
    public <E extends Exception> ResultOrRTE<T, E> specError(
        ExceptionWrapper<E> exceptionMapper
    ) {
        return ResultOrRTE.of(this.result, exceptionMapper);
    }

    @Override
    public <E extends Exception> ResultOrRTE<T, E> specError(
        Class<E> exceptionClass,
        Function<Exception, E> wrapper
    ) {
        return ResultOrRTE.of(this.result, new ExceptionWrapper<>(exceptionClass, wrapper));
    }
}
