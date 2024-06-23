package org.xyp.sample.spring.webapi.repository.jdbc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;

@Repository
public interface TaskDaoJdbc extends ListCrudRepository<Task, Task.TaskId> {

//    List<Task> findTasks(AggregateReference<Batch, Long> batchId);
}
