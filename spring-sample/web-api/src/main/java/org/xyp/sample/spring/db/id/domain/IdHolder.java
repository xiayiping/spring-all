package org.xyp.sample.spring.db.id.domain;

public interface IdHolder<ID, T> {
    IdHolder<ID, T> withId(ID id);
}
