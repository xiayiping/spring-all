package org.xyp.sample.spring.db.id.generator;

public class IdGenerationException extends RuntimeException {
    public IdGenerationException(Throwable cause) {
        super(cause);
    }

    public IdGenerationException(String message) {
        super(message);
    }
}
