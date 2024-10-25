package org.xyp.id;

import java.util.List;

public interface IdGenerator<I> {

    I nextId(
        String entityName,
        JdbcConnectionAccessorFactory connectionFactory
    );

    List<I> nextId(
        String entityName,
        int fetchSize,
        JdbcConnectionAccessorFactory connectionFactory
    );

    List<I> nextId(
        String entityName,
        int fetchSize,
        int defaultStepSize,
        int defaultFetchSize,
        JdbcConnectionAccessorFactory connectionFactory
    );
}
