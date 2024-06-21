package org.xyp.sample.spring.db.id;

public class IdGenerationException extends RuntimeException {
    public IdGenerationException(Throwable cause) {
        super(cause);
    }

    public IdGenerationException(String message) {
        super(message);
    }
}
