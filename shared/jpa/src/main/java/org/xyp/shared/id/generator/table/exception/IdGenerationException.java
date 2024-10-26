package org.xyp.shared.id.generator.table.exception;

import java.util.Optional;

public class IdGenerationException extends RuntimeException {
    public IdGenerationException(String entityName, Throwable cause) {
        super("error create id for " + entityName + ", due to " +
                Optional.ofNullable(cause).map(Throwable::getMessage).orElse("null")
            , cause);
    }
}
