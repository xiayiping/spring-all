package org.xyp.function.wrapper;

import org.xyp.function.*;

import java.util.function.Function;

public final class WithCloseable<C extends AutoCloseable, T> {

    public static <L extends AutoCloseable> WithCloseable<L, L> open(ExceptionalSupplier<L> open) {
        return new WithCloseable<>(
            open,
            c -> c
        );
    }

    final ExceptionalSupplier<C> closeableSupplier;
    final ExceptionalFunction<? super C, ? extends T> innerMapper;

    private WithCloseable(
        ExceptionalSupplier<C> closeableSupplier,
        ExceptionalFunction<? super C, ? extends T> mapper
    ) {
        this.closeableSupplier = closeableSupplier;
        this.innerMapper = mapper;
    }

    public <U> WithCloseable<C, U> map(ExceptionalFunction<? super T, ? extends U> function) {
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final T inner = innerMapper.apply(closeable);
                if (null != inner)
                    return function.apply(inner);
                return null;
            }
        );
    }

    public WithCloseable<C, T> consume(ExceptionalConsumer<? super T> consumer) {
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                final T item = innerMapper.apply(closeable);
                if (null != item)
                    consumer.accept(item);
                return item;
            }
        );
    }

    public <U> WithCloseable<C, U> mapWithCloseable(ExceptionalBiFunction<? super C, ? super T, ? extends U> biFunction) {
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> biFunction.apply(closeable, innerMapper.apply(closeable))
        );
    }

    public T closeAndGet() {
        try (var closeable = closeableSupplier.get()) {
            return innerMapper.apply(closeable);
        } catch (Exception e) {
            throw Fun.convertRte(e, RuntimeException.class, FunctionException::new);
        }
    }

    public <E extends RuntimeException> T closeAndGet(
        Class<E> target,
        Function<Throwable, E> exceptionMapper
    ) {
        try (var closeable = closeableSupplier.get()) {
            return innerMapper.apply(closeable);
        } catch (Throwable e) {
            throw Fun.convertRte(e, target, exceptionMapper);
        }
    }

    public Result<T, Throwable> closeAndGetResult() {
        try (var closeable = closeableSupplier.get()) {
            return Result.success(innerMapper.apply(closeable));
        } catch (Throwable e) {
            return Result.failure(e);
        }
    }

    public <E extends RuntimeException> Result<T, E> closeAndGetResult(
        Class<E> target,
        Function<Throwable, E> exceptionMapper
    ) {
        try (var closeable = closeableSupplier.get()) {
            return Result.success(innerMapper.apply(closeable));
        } catch (Throwable e) {
            return Result.failure(Fun.convertRte(e, target, exceptionMapper));
        }
    }
}
