package org.xyp.sample.spring.webapi.repository.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.jdbc.Batch;
import org.xyp.sample.spring.webapi.domain.entity.jdbc.Task;

import java.util.List;

@Repository
public interface TaskDaoJdbc extends ListCrudRepository<Task, Long> {

    List<Task> findTasksByBatch(AggregateReference<Batch, Long> batchId);
}
