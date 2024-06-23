package org.xyp.sample.spring.db.id.generator;

import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;

import java.util.List;

public interface IdGenerator<I> {

    String GENERATOR_NAME = "GENERATOR_NAME";
    String FETCH_SIZE = "increment_size";

    I nextId(String entityName, /*Class<W> idWrapperClass,*/
             String dialect, JdbcConnectionAccessorFactory connectionFactory);

    List<I> nextId(String entityName, /*Class<W> idWrapperClass,*/ int fetchSize,
                   String dialect, JdbcConnectionAccessorFactory connectionFactory);
}
