package org.xyp.sample.spring.webapi.repository.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;

import java.util.Optional;

@Repository
public interface BatchDaoJdbc extends ListCrudRepository<Batch, Batch.BatchId> {

    @Query("SELECT b.*, br.* FROM batch b left join test.batch_rule br on b.id = br.batch_id WHERE b.id = :batchId")
    Optional<Batch> findWithRuleById(@Param("batchId")Batch.BatchId batchId);
}
