package org.xyp.spring.datajdbc.domain.repository;

import org.xyp.spring.datajdbc.domain.pojo.Batch;

import java.util.Collection;
import java.util.Optional;

public interface BatchRepository {

    Optional<Batch> get(Batch.Id id);

    Batch save(Batch batch);

    Collection<Batch> saveAll(Collection<Batch> batches);
}
