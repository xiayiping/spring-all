package org.xyp.sample.spring.db.id.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;

public interface HasId<T, I> {
    void refreshId(I id);

    @JsonIgnore
    String identityGeneratorName();

    // due to data-jdbc doesn't need id for leaf, do this tricky to set all leave's id from aggregate root
    default Collection<? extends HasId<?, I>> leaves() {
        return null;
    }

    I peekId();
}
