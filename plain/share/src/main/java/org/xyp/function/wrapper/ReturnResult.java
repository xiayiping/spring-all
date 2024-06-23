package org.xyp.function.wrapper;

import org.xyp.function.Fun;
import org.xyp.function.FunctionException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReturnResult<R, E extends Exception> {
    private final R result;
    private final E exception;

    private ReturnResult(R result) {
        this.result = result;
        this.exception = null;
    }

    public static <R, E extends Exception> ReturnResult<R, E> ofResult(R result) {
        return new ReturnResult<>(result);
    }

    public ReturnResult(E exception) {
        this.result = null;
        this.exception = exception;
    }

    public static <R, E extends Exception> ReturnResult<R, E> ofError(E exception) {
        return new ReturnResult<>(exception);
    }

    public R get() {
        if (null != exception) {
            throw Fun.convertRte(exception, FunctionException.class, FunctionException::new);
        }
        return result;
    }

    public <RTE extends RuntimeException> R get(Function<E, RTE> exceptionMapper) {
        if (null != exception) {
            throw exceptionMapper.apply(exception);
        }
        return result;
    }

    public Optional<R> getOption() {
        if (null != exception) {
            throw Fun.convertRte(exception, FunctionException.class, FunctionException::new);
        }
        return Optional.ofNullable(result);
    }

    public <RTE extends RuntimeException> Optional<R> getOption(Function<E, RTE> exceptionMapper) {
        if (null != exception) {
            throw exceptionMapper.apply(exception);
        }
        return Optional.ofNullable(result);
    }

    public <RTE extends RuntimeException> ReturnResult<R, RTE> specError(Class<RTE> target, Function<Exception, RTE> exceptionMapper) {
        if (null != exception) {
            return ReturnResult.ofError(Fun.convertRte(exception, target, exceptionMapper));
        }
        return ReturnResult.ofResult(result);
    }

    public void ifError(Consumer<E> consumer) {
        if (null != exception) {
            consumer.accept(exception);
        }
    }
}
