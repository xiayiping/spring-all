package org.xyp.id;

import java.util.List;

public interface IdGenerator<I> {

    String GENERATOR_NAME = "GENERATOR_NAME";

    I nextId(
        String entityName,
        JdbcConnectionAccessorFactory connectionFactory
    );

    List<I> nextId(
        String entityName,
        int fetchSize,
        JdbcConnectionAccessorFactory connectionFactory
    );
}
