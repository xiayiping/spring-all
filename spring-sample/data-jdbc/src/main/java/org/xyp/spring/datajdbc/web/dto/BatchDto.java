package org.xyp.spring.datajdbc.web.dto;

import org.xyp.spring.datajdbc.domain.pojo.Batch;

import java.util.Optional;

public record BatchDto(
    Long id,
    String name
) {

    public Batch toBatch() {
        return new Batch(Batch.Id.of(id), name);
    }

    public static BatchDto of(Batch batch) {
        return new BatchDto(
            Optional.ofNullable(batch.id())
                .map(Batch.Id::id)
                .orElse(null)
            ,
            batch.name()
        );
    }
}
