package org.xyp.sample.spring.db.id.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.Collections;

public interface HasId<I> {
    void putGeneratedId(I id);

    I peekId();

    @JsonIgnore
    String identityGeneratorName();

    // due to data-jdbc doesn't need id for leaf, do this tricky to set all leave's id from aggregate root
    default <E extends HasId<I>> Collection<E> leaves() {
        return Collections.emptyList();
    }

}
