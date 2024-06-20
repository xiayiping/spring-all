package org.xyp.function.wrapper.exceptional;

import org.xyp.function.ExceptionalFunction;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FunctionSuccessSpecErr<T, E extends Exception> implements ResultOrErrorWrapper<T, E> {

    private final T result;
    private final ExceptionWrapper<E> wrapper;

    public FunctionSuccessSpecErr(T result, ExceptionWrapper<E> wrapper) {
        this.result = result;
        this.wrapper = wrapper;
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
    public ResultOrErrorWrapper<T, E> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        try {
            return predicate.test(result) ? this : new FunctionSuccessSpecErr<>(null, wrapper);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrErrorWrapper<T, E>) new FunctionErrorSpecErr<>(e, wrapper);
            return re;
        }
    }

    @Override
    public <U> ResultOrErrorWrapper<U, E> map(ExceptionalFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccessSpecErr<>(null, wrapper);
            }
            U u = mapper.apply(result);
            return new FunctionSuccessSpecErr<>(u, wrapper);
        } catch (Exception e) {
            return new FunctionErrorSpecErr<>(e, wrapper);
        }
    }

    @Override
    public <U> ResultOrErrorWrapper<U, E> flatMap(ExceptionalFunction<? super T, ? extends ResultOrErrorWrapper<? extends U, E>> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccessSpecErr<>(null, wrapper);
            }
            @SuppressWarnings("unchecked")
            val r = (ResultOrErrorWrapper<U, E>) mapper.apply(result);
            return r;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrErrorWrapper<U, E>) new FunctionErrorSpecErr<>(e, wrapper);
            return re;
        }
    }

    @Override
    public <U> ResultOrErrorWrapper<U, E> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccessSpecErr<>(null, wrapper);
            }
            val r = mapper.apply(result);
            return ResultOrErrorWrapper.onOpt(() -> r, wrapper);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrErrorWrapper<U, E>) new FunctionErrorSpecErr<>(e, wrapper);
            return re;
        }
    }

    @Override
    public Stream<T> stream() {
        return Stream.ofNullable(result);
    }


    @Override
    public ResultOrErrorWrapper<T, E> ifPresent(Consumer<? super T> action) {
        try {
            if (null != result) {
                action.accept(result);
            }
            return this;
        } catch (Exception e) {
            return new FunctionErrorSpecErr<>(e, wrapper);
        }
    }

    @Override
    public ResultOrErrorWrapper<T, E> ifError(Consumer<? super E> errAction) {
        return this;
    }

    @Override
    public ResultOrErrorWrapper<T, E> ifEmpty(Runnable emptyAction) {
        try {
            if (null == result) {
                emptyAction.run();
            }
            return this;
        } catch (Exception e) {
            return new FunctionErrorSpecErr<>(e, wrapper);
        }
    }

    @Override
    public T get() {
        return result;
    }

    @Override
    public T get(Function<Exception, ? extends T> exceptionMapper) {
        return result;
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
    public E getException() {
        return null;
    }
}
