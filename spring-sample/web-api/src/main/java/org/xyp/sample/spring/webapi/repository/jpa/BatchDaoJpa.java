package org.xyp.sample.spring.webapi.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.tracing.Trace;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;

import java.util.Optional;

@Repository
public interface BatchDaoJpa extends ListCrudRepository<Batch, Batch.BatchId> {

    @Trace(value = "batch.findWithRulesById")
    // careful: org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
    @Query("""
        select bt from Batch bt
        left join fetch bt.batchRules r
        where bt.id = :id
        """)
    Optional<Batch> findWithRulesById(@Param("id") Batch.BatchId batchId);
}
