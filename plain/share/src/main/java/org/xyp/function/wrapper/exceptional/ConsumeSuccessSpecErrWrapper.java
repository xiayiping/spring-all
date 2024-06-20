package org.xyp.function.wrapper.exceptional;

import java.util.function.Consumer;

public class ConsumeSuccessSpecErrWrapper<E extends Exception> implements ConsumeResultErrWrapper<E> {


    public ConsumeSuccessSpecErrWrapper(
        ExceptionWrapper<E> ignored
    ) {
    }

    public boolean isError() {
        return false;
    }

    public void throwIfError() {
        // success so no need do anything
    }

    public void ifError(Consumer<Exception> errorConsumer) {
        // success so no need do anything
    }

    @Override
    public E getException() {
        return null;
    }
}
