package org.xyp.function.wrapper.exceptional;

import java.util.function.Consumer;

public class ConsumeErrorWrapper<E extends Exception> implements ConsumeResultErrWrapper<E> {

    final Exception exception;
    private final ExceptionWrapper<E> wrapper;

    public ConsumeErrorWrapper(
        Exception exception,
        ExceptionWrapper<E> wrapper
    ) {
        this.exception = exception;
        this.wrapper = wrapper;
    }

    public boolean isError() {
        return true;
    }

    public void throwIfError() throws E {
        throw wrapper.wrap(exception);
    }

    public void ifError(Consumer<Exception> errorConsumer) {
        errorConsumer.accept(exception);
    }


    @Override
    public E getException() {
        return wrapper.wrap(exception);
    }
}
