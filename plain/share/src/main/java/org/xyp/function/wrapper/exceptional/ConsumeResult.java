package org.xyp.function.wrapper.exceptional;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ConsumeResult {

    boolean isError();

    void throwIfError(Function<Exception, RuntimeException> exceptionMapper);

    void throwIfError() throws Exception;

    ConsumeResult ifError(Consumer<Exception> errorConsumer);

    <E extends Exception> ConsumeResultErrWrapper<E> specError(
        ExceptionWrapper<E> exceptionMapper
    );
}
