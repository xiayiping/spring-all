package org.xyp.sample.spring.db.id;

import java.sql.Connection;
import java.util.List;

public interface IdGenerator<I> {

    <T, W> W nextId(Class<T> entityClass, Class<W> idWrapperClass,
                    String dialect, JdbcConnectionAccessorFactory connectionFactory);

    <T, W> List<W> nextId(Class<T> entityClass, Class<W> idWrapperClass, int fetchSize,
                          String dialect, JdbcConnectionAccessorFactory connectionFactory);
}
