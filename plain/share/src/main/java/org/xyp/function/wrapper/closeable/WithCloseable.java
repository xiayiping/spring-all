package org.xyp.function.wrapper.closeable;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.xyp.function.*;
import org.xyp.function.wrapper.exceptional.ConsumeError;
import org.xyp.function.wrapper.exceptional.ConsumeResult;
import org.xyp.function.wrapper.exceptional.ConsumeSuccess;
import org.xyp.function.wrapper.exceptional.ResultOrError;

import java.io.Closeable;
import java.util.Objects;
import java.util.function.BiFunction;

@Slf4j
public class WithCloseable<T, C extends AutoCloseable> {

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

    public <U> WithCloseable<U, C> map(ExceptionalFunction<? super T, ? extends U> function) {
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> function.apply(innerMapper.apply(closeable))
        );
    }

    public WithCloseable<T, C> consume(ExceptionalConsumer<? super T> consumer) {
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> {
                val item = innerMapper.apply(closeable);
                consumer.accept(item);
                return item;
            }
        );
    }

    public <U> WithCloseable<U, C> mapWithCloseable(ExceptionalBiFunction<? super C, ? super T, ? extends U> biFunction) {
        return new WithCloseable<>(
            closeableSupplier,
            closeable -> biFunction.apply(closeable, innerMapper.apply(closeable))
        );
    }

    public ResultOrError<T> closeAndGet() {
        try (var closeable = closeableSupplier.get()) {
            return ResultOrError.of(innerMapper.apply(closeable));
        } catch (Exception e) {
            return ResultOrError.ofErr(e);
        }
    }

    public ConsumeResult close() {
        try (var closeable = closeableSupplier.get()) {
            innerMapper.apply(closeable);
            return new ConsumeSuccess();
        } catch (Exception e) {
            return new ConsumeError(e);
        }
    }

    public T closeAndGet(BiFunction<? super C, ? super Exception, ? extends T> catcher) {
        Objects.requireNonNull(catcher);
        C closeableHolder = null;
        try (var closeable = closeableSupplier.get()) {
            closeableHolder = closeable;
            return innerCatch(catcher, closeable, closeableHolder);
        } catch (Exception e) {
            throw new FunctionException(e);
        }
    }

    private T innerCatch(BiFunction<? super C, ? super Exception, ? extends T> catcher, C closeable, C closeableHolder) {
        try {
            return innerMapper.apply(closeable);
        } catch (Exception e) {
            log.warn("suppress ex in with closeable ", e);
            return catcher.apply(closeableHolder, e);
        }
    }
}
