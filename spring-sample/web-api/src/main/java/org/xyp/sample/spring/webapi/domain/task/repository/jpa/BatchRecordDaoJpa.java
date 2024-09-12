package org.xyp.sample.spring.webapi.domain.task.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.BatchRecord;

import java.util.Optional;

@Repository
public interface BatchRecordDaoJpa extends ListCrudRepository<BatchRecord, Batch.BatchId> {

    @Query("""
        select bt from BatchRecord bt
        left join fetch bt.batchRules r
        where bt.id = :id
        """)
    Optional<BatchRecord> findWithRulesById(@Param("id") Batch.BatchId batchId);
}
