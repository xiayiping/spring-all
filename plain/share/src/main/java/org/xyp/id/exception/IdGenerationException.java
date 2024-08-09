package org.xyp.id.exception;

public class IdGenerationException extends RuntimeException {
    public IdGenerationException(Throwable cause) {
        super(cause);
    }

    public IdGenerationException(String message) {
        super(message);
    }
}
