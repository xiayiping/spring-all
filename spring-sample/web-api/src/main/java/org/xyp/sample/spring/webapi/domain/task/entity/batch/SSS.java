package org.xyp.sample.spring.webapi.domain.task.entity.batch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public record SSS(
    @Id
    Long id,
    String s,
    @OneToMany
    Set<Batch> batches
) {


}
