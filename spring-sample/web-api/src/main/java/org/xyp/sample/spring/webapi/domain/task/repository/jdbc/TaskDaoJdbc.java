package org.xyp.sample.spring.webapi.domain.task.repository.jdbc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.task.entity.task.Task;

@Repository
public interface TaskDaoJdbc extends ListCrudRepository<Task, Task.TaskId> {

//    List<Task> findTasks(AggregateReference<Batch, Long> batchId);
}
