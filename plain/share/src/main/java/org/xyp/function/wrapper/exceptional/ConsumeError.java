package org.xyp.function.wrapper.exceptional;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConsumeError implements ConsumeResult {

    final Exception exception;

    public ConsumeError(Exception exception) {
        this.exception = exception;
    }

    public boolean isError() {
        return true;
    }

    public void throwIfError(Function<Exception, RuntimeException> exceptionMapper
    ) {
        throw exceptionMapper.apply(exception);
    }

    public void throwIfError() throws Exception {
        throw exception;
    }

    public ConsumeResult ifError(Consumer<Exception> errorConsumer) {
        errorConsumer.accept(exception);
        return this;
    }

    public <E extends Exception> ConsumeResultErrWrapper<E> specError(
        ExceptionWrapper<E> exceptionMapper
    ) {
        return new ConsumeErrorWrapper<>(exception, exceptionMapper);
    }
}
