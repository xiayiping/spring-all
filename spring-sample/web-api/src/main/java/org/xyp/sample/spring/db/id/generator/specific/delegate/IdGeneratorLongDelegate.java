package org.xyp.sample.spring.db.id.generator.specific.delegate;

import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;

import java.util.List;

public interface IdGeneratorLongDelegate {
    <T> List<Long> nextId(Class<T> entityClass, int fetchSize, String dialect, JdbcConnectionAccessorFactory factory);
}
