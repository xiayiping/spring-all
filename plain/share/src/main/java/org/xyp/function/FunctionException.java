package org.xyp.function;

public class FunctionException extends RuntimeException {
    public FunctionException() {
    }

    public FunctionException(String message) {
        super(message);
    }

    public FunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FunctionException(Throwable cause) {
        super(cause);
    }
}
