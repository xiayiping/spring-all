package org.xyp.function.wrapper;

import lombok.val;
import org.xyp.function.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * map 函数为递归 lazy 调用，可以保留链式调用的调用栈<br/>
 * 返回值一旦有null则调用链中断<br/>
 * 由于是 lazy 调用， 因此最后需要用 getXXX 函数做真正的调用
 *
 * @param <R>
 */
public class ResultOrError<R> {

    private final ExceptionalSupplier<R> supplier;

    private ResultOrError(ExceptionalSupplier<R> supplier) {
        this.supplier = supplier;
    }

    public static <T1> ResultOrError<T1> of(T1 t1) {
        return new ResultOrError<>(() -> t1);
    }

    public static <R> ResultOrError<R> on(ExceptionalSupplier<R> supplier) {
        return new ResultOrError<>(supplier);
    }

    public ResultOrError<R> filter(Predicate<? super R> predicate) {
        return new ResultOrError<>(
            () -> {
                val innerResult = supplier.get();
                if (null != innerResult && predicate.test(innerResult)) {
                    return innerResult;
                }
                return null;
            }
        );
    }

    public ResultOrError<Optional<R>> mapToOptional(Predicate<? super R> predicate) {
        return new ResultOrError<>(
            () -> Optional.ofNullable(supplier.get())
        );
    }

    public <U> ResultOrError<U> map(ExceptionalFunction<? super R, ? extends U> mapper) {
        return new ResultOrError<>(
            () -> {
                val innerResult = supplier.get();
                if (null != innerResult) {
                    return mapper.apply(innerResult);
                }
                return null;
            }
        );
    }

    public R get() {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new FunctionException(e);
        }
    }

    public <E extends RuntimeException> R get(Class<E> target, Function<Exception, E> exceptionMapper) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw Fun.convertRte(e, target, exceptionMapper);
        }
    }

    public ReturnResult<R, FunctionException> getResult() {
        try {
            return ReturnResult.ofResult(supplier.get());
        } catch (Exception e) {
            return ReturnResult.ofError(Fun.convertRte(
                e,
                FunctionException.class,
                FunctionException::new
            ));
        }
    }

    public <E extends RuntimeException> ReturnResult<R, E> getResult(Class<E> target, Function<Exception, E> exceptionMapper) {
        try {
            return ReturnResult.ofResult(supplier.get());
        } catch (Exception e) {
            return ReturnResult.ofError(Fun.convertRte(e, target, exceptionMapper));
        }
    }
}
