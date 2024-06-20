package org.xyp.sample.spring.webapi.repository.jpa;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.jpa.Task;

@Repository
public interface TaskDaoJpa extends ListCrudRepository<Task, Task.TaskId> {
}
