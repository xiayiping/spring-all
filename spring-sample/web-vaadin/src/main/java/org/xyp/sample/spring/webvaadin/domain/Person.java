package org.xyp.sample.spring.webvaadin.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;

@Builder
public record Person(
    @JsonUnwrapped
    PersonId id,
    String name
) {
    public record PersonId(long id) {
        public static PersonId of(long id) {
            return new PersonId(id);
        }
    }
}
