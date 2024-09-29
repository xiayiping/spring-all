package org.xyp.spring.datajdbc.domain.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.xyp.spring.datajdbc.domain.entity.BatchEntity;

import java.util.Optional;

public record Batch(
    Id id,
    String name
) {

    public static Batch of(BatchEntity entity) {
        return new Batch(Id.of(entity.getId()), entity.getName());
    }

    public BatchEntity toEntity() {
        return new BatchEntity(
            Optional.ofNullable(id).map(id -> id.id).orElse(null),
            name
        );
    }

    public record Id(long id) {
        public static Id of(Long id) {
            return null == id ? null : new Id(id);
        }
    }
}
