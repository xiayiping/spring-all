package org.xyp.function.wrapper.exceptional;

import lombok.NoArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;

@NoArgsConstructor
public class ConsumeSuccess implements ConsumeResult {

    public boolean isError() {
        return false;
    }

    public void throwIfError(Function<Exception, RuntimeException> exceptionMapper) {
        // success so no need do anything
    }

    public void throwIfError() {
        // nothing to implement for success
    }

    public ConsumeResult ifError(Consumer<Exception> errorConsumer) {
        // success so no need do anything
        return this;
    }

    public <E extends Exception> ConsumeResultErrWrapper<E> specError(
        ExceptionWrapper<E> exceptionMapper
    ) {
        return new ConsumeSuccessSpecErrWrapper<>(exceptionMapper);
    }
}
