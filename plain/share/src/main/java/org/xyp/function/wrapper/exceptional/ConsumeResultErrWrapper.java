package org.xyp.function.wrapper.exceptional;

import java.util.function.Consumer;

public interface ConsumeResultErrWrapper<E extends Exception> {

    boolean isError();

    void throwIfError() throws E;

    void ifError(Consumer<Exception> errorConsumer);

    E getException();
}
