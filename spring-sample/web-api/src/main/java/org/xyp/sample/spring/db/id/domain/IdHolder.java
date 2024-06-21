package org.xyp.sample.spring.db.id.domain;

public interface IdHolder<I, T> {
    IdHolder<I, T> withId(I id);
}
