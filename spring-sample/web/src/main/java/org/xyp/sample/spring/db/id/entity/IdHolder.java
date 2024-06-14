package org.xyp.sample.spring.db.id.entity;

public interface IdHolder<ID, T> {
    IdHolder<ID, T> withId(ID id);
}
