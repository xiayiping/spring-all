package org.xyp.spring.datajdbc.domain.pojo;

import lombok.Builder;
import org.xyp.spring.datajdbc.domain.entity.BatchDescEntity;
import org.xyp.spring.datajdbc.domain.entity.BatchEntity;

import java.util.Optional;

@Builder
public record Batch(
    Id id,
    String name,
    String desc
) {

    public static Batch of(BatchEntity entity) {
        return new Batch(
            Id.of(entity.getId()),
            entity.getName(),
            Optional.ofNullable(entity.getDesc()).map(BatchDescEntity::getDesc).orElse(null)
        );
    }

    public BatchEntity toEntity() {
        return new BatchEntity(
            Optional.ofNullable(id).map(id -> id.value).orElse(null),
            name,
            new BatchDescEntity(desc)
        );
    }

    public record Id(long value) {
        public static Id of(Long id) {
            return null == id ? null : new Id(id);
        }
    }
}
