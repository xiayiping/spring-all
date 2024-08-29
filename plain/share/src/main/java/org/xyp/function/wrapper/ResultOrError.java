package org.xyp.function.wrapper;

import org.xyp.function.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * map 函数为递归 lazy 调用，可以保留链式调用的调用栈<br/>
 * 返回值一旦有null则调用链中断<br/>
 * 由于是 lazy 调用， 因此最后需要用 getXXX 函数做真正的调用
 *
 * @param <R>
 */
public final class ResultOrError<R> {

    private final ExceptionalSupplier<R> supplier;
    private final StackStepInfo stackStepInfo;

    private ResultOrError(ExceptionalSupplier<R> supplier, StackStepInfo stackStep) {
        this.supplier = supplier;
        this.stackStepInfo = stackStep;
    }

    private static StackWalker.StackFrame getStackStep() {
        final var stack = StackWalker.getInstance()
            .walk(stream -> stream.filter(s -> !s.toStackTraceElement().getClassName().equals(ResultOrError.class.getName()))
                .findFirst())
            .orElse(null);
        return stack;
    }

    public static <T1> ResultOrError<T1> of(T1 t1) {
        final var stackInfo = new StackStepInfo(getStackStep(), null);
        return new ResultOrError<>(() -> {
            stackInfo.setOutput(t1);
            return t1;
        }, stackInfo);
    }

    public static <R> ResultOrError<R> on(ExceptionalSupplier<R> supplier) {
        final var stackInfo = new StackStepInfo(getStackStep(), null);
        return new ResultOrError<>(() -> {
            final var result = supplier.get();
            stackInfo.setOutput(result);
            return result;
        }, stackInfo);
    }

    public static ResultOrError<Void> doRun(ExceptionalRunnable runner) {
        final var stackInfo = new StackStepInfo(getStackStep(), null);
        return new ResultOrError<>(() -> {
            runner.run();
            stackInfo.setOutput(null);
            return null;
        }, stackInfo
        );
    }

    public ResultOrError<R> filter(Predicate<? super R> predicate) {
        final var stackInfo = new StackStepInfo(getStackStep(), this.stackStepInfo);
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                stackInfo.setInput(innerResult);
                final var test = null != innerResult && predicate.test(innerResult);
                stackInfo.setOutput(test);
                if (test) {
                    return innerResult;
                }
                return null;
            }, stackInfo
        );
    }

    public ResultOrError<R> fallbackForEmpty(Supplier<R> emptySupplier) {
        final var stackInfo = new StackStepInfo(getStackStep(), this.stackStepInfo);
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                stackInfo.setInput(innerResult);
                if (null != innerResult) {
                    stackInfo.setOutput(innerResult);
                    return innerResult;
                }
                final var res = emptySupplier.get();
                stackInfo.setOutput(res);
                return res;
            }, stackInfo
        );
    }

    public ResultOrError<R> consume(ExceptionalConsumer<? super R> consumer) {
        final var stackInfo = new StackStepInfo(getStackStep(), this.stackStepInfo);
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                stackInfo.setInput(innerResult);
                if (null != innerResult) {
                    consumer.accept(innerResult);
                }
                stackInfo.setOutput(innerResult);
                return innerResult;
            }, stackInfo
        );
    }

    public ResultOrError<R> logTrace(Consumer<String> logger) {
        final var stackInfo = new StackStepInfo(getStackStep(), this.stackStepInfo);
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                stackInfo.setInput(innerResult);
                stackInfo.setOutput(innerResult);
                if (null != innerResult) {
                    var currentStackInfo = stackInfo;
                    while (null != currentStackInfo) {
                        final var stack = currentStackInfo.getStackFrame().toString();
                        final var input = Optional.of(Objects.toString(currentStackInfo.getInput()))
                            .map(s -> {
                                if (s.length() > 100) {
                                    return s.substring(0, 100) + " ...";
                                }
                                return s;
                            });
                        final var output = Optional.of(Objects.toString(currentStackInfo.getOutput()))
                            .map(s -> {
                                if (s.length() > 100) {
                                    return s.substring(0, 100) + " ...";
                                }
                                return s;
                            });
                        final var log = String.format("%s\n    input: %s\n    output: %s", stack, input, output);
                        logger.accept(log);
                        currentStackInfo = currentStackInfo.getPrevious();
                    }
                }
                return innerResult;
            }, stackInfo
        );
    }

    public <U> ResultOrError<U> map(ExceptionalFunction<? super R, ? extends U> mapper) {
        final var stackInfo = new StackStepInfo(getStackStep(), this.stackStepInfo);
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                stackInfo.setInput(innerResult);
                if (null != innerResult) {
                    final var res = mapper.apply(innerResult);
                    stackInfo.setOutput(res);
                    return res;
                }
                return null;
            }, stackInfo
        );
    }

    public <U> ResultOrError<U> noExMap(Function<? super R, ? extends U> mapper) {
        return map(mapper::apply);
    }

    public <U> ResultOrError<U> flatMap(ExceptionalFunction<? super R, ResultOrError<U>> mapper) {
        final var stackInfo = new StackStepInfo(getStackStep(), this.stackStepInfo);
        return new ResultOrError<>(
            () -> {
                final R innerResult = supplier.get();
                stackInfo.setInput(innerResult);
                if (null != innerResult) {
                    final ResultOrError<U> optional = mapper.apply(innerResult);
                    final var res = optional.get();
                    stackInfo.setOutput(res);
                    return res;
                }
                return null;
            }, stackInfo
        );
    }

    public R get() {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw Fun.convertRte(e, RuntimeException.class, FunctionException::new);
        }
    }

    public Optional<R> getOption() {
        return Optional.ofNullable(get());
    }

    public <E extends RuntimeException> R getOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw Fun.convertRte(e, target, exceptionMapper);
        }
    }

    public <E extends RuntimeException> Optional<R> getOptionOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        return Optional.ofNullable(getOrSpecError(target, exceptionMapper));
    }

    public Result<R, Throwable> getResult() {
        try {
            return Result.success(supplier.get(), stackStepInfo);
        } catch (Throwable e) {
            return Result.failure(e);
        }
    }

    public <E extends RuntimeException> Result<R, E> getResultOrSpecError(Class<E> target, Function<Throwable, E> exceptionMapper) {
        try {
            return Result.success(supplier.get(), stackStepInfo);
        } catch (Throwable e) {
            return Result.failure(Fun.convertRte(e, target, exceptionMapper));
        }
    }
}
