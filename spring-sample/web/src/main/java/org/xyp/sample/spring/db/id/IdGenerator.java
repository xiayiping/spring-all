package org.xyp.sample.spring.db.id;

public interface IdGenerator<ID> {
    <T> ID nextId(Class<T> entityClass);
}
