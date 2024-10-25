package org.xyp.spring.datajdbc.web.dto;

import org.xyp.spring.datajdbc.domain.pojo.Batch;

import java.util.Optional;

public record BatchDto(
    Long id,
    String name,
    String desc
) {

    public Batch toBatch() {
        return new Batch(Batch.Id.of(id), name, desc);
    }

    public static BatchDto of(Batch batch) {
        return new BatchDto(
            Optional.ofNullable(batch.id())
                .map(Batch.Id::value)
                .orElse(null)
            ,
            batch.name(),
            batch.desc()
        );
    }
}
