package org.xyp.function.wrapper.exceptional;

import org.xyp.function.ExceptionalFunction;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FunctionErrorSpecErr<T, E extends Exception> implements ResultOrErrorWrapper<T, E> {

    private final Exception error;
    private final ExceptionWrapper<E> wrapper;

    public FunctionErrorSpecErr(
        Exception error,
        ExceptionWrapper<E> wrapper
    ) {
        this.error = error;
        this.wrapper = wrapper;
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
    public ResultOrErrorWrapper<T, E> ifPresent(Consumer<? super T> action) {
        return this;
    }

    @Override
    public ResultOrErrorWrapper<T, E> ifError(Consumer<? super E> errAction) {
        try {
            errAction.accept(wrapper.wrap(error));
            return this;
        } catch (Exception e) {
            return new FunctionErrorSpecErr<>(e, wrapper);
        }
    }

    @Override
    public ResultOrErrorWrapper<T, E> ifEmpty(Runnable emptyAction) {
        return this;
    }

    @Override
    public ResultOrErrorWrapper<T, E> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this;
    }

    @Override
    public <U> ResultOrErrorWrapper<U, E> map(ExceptionalFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        @SuppressWarnings("unchecked")
        val r = (ResultOrErrorWrapper<U, E>) this;
        return r;
    }

    @Override
    public <U> ResultOrErrorWrapper<U, E> flatMap(
        ExceptionalFunction<? super T, ? extends ResultOrErrorWrapper<? extends U, E>> mapper
    ) {
        Objects.requireNonNull(mapper);
        @SuppressWarnings("unchecked")
        val r = (ResultOrErrorWrapper<U, E>) this;
        return r;
    }

    @Override
    public <U> ResultOrErrorWrapper<U, E> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        @SuppressWarnings("unchecked")
        val r = (ResultOrErrorWrapper<U, E>) this;
        return r;
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public T get() throws E {
        throw wrapper.wrap(error);
    }

    @Override
    public T get(Function<Exception, ? extends T> exceptionMapper) {
        return exceptionMapper.apply(error);
    }

    @Override
    public Optional<T> getOptional() throws E {
        return Optional.empty();
    }

    @Override
    public Optional<T> getOptional(Function<Exception, T> exceptionMapper) {
        return Optional.ofNullable(exceptionMapper.apply(error));
    }

    @Override
    public E getException() {
        return wrapper.wrap(error);
    }
}
