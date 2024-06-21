package org.xyp.function.wrapper.exceptional;

import org.xyp.function.ExceptionalFunction;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FunctionSuccessSpecErr<T, E extends Exception> implements ResultOrRTE<T, E> {

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
    public ResultOrRTE<T, E> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        try {
            return predicate.test(result) ? this : new FunctionSuccessSpecErr<>(null, wrapper);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrRTE<T, E>) new FunctionErrorSpecErr<>(e, wrapper);
            return re;
        }
    }

    @Override
    public <U> ResultOrRTE<U, E> map(ExceptionalFunction<? super T, ? extends U> mapper) {
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
    public <U> ResultOrRTE<U, E> flatMap(ExceptionalFunction<? super T, ? extends ResultOrRTE<? extends U, E>> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccessSpecErr<>(null, wrapper);
            }
            @SuppressWarnings("unchecked")
            val r = (ResultOrRTE<U, E>) mapper.apply(result);
            return r;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrRTE<U, E>) new FunctionErrorSpecErr<>(e, wrapper);
            return re;
        }
    }

    @Override
    public <U> ResultOrRTE<U, E> flatMapOpt(ExceptionalFunction<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        try {
            if (null == result) {
                return new FunctionSuccessSpecErr<>(null, wrapper);
            }
            val r = mapper.apply(result);
            return ResultOrRTE.onOpt(() -> r, wrapper);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            val re = (ResultOrRTE<U, E>) new FunctionErrorSpecErr<>(e, wrapper);
            return re;
        }
    }

    @Override
    public Stream<T> stream() {
        return Stream.ofNullable(result);
    }


    @Override
    public ResultOrRTE<T, E> ifPresent(Consumer<? super T> action) {
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
    public ResultOrRTE<T, E> ifError(Consumer<? super E> errAction) {
        return this;
    }

    @Override
    public ResultOrRTE<T, E> ifEmpty(Runnable emptyAction) {
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
