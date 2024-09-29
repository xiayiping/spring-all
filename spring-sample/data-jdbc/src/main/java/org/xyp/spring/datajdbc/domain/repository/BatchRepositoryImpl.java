package org.xyp.spring.datajdbc.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.xyp.spring.datajdbc.domain.entity.BatchEntity;
import org.xyp.spring.datajdbc.domain.pojo.Batch;
import org.xyp.spring.datajdbc.domain.repository.jdbc.BatchDao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class BatchRepositoryImpl implements BatchRepository {

    final BatchDao batchDao;

    @Override
    public Optional<Batch> get(Batch.Id id) {
        return batchDao.findById(id.id())
            .map(Batch::of)
            ;
    }

    @Override
    public Batch save(Batch batch) {
        return Optional.of(batchDao.save(batch.toEntity()))
            .map(Batch::of)
            .orElse(null)
            ;
    }

    @Override
    public Collection<Batch> saveAll(Collection<Batch> batches) {
        return batchDao.saveAll(batches.stream().map(Batch::toEntity).toList())
            .stream()
            .map(Batch::of)
            .toList()
            ;
    }
}
