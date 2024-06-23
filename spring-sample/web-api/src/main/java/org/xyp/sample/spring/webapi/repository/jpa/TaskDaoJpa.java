package org.xyp.sample.spring.webapi.repository.jpa;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDaoJpa extends ListCrudRepository<Task, Task.TaskId> {
//    @Query("select t from Task t left join fetch t.batch where t.id = :taskId")
        //left join fetch t.batch.tasks
    Optional<Task> findWithBatchById(@Param("taskId") Task.TaskId taskId);

//    @Query("select t from Task t left join fetch t.batch where t.id in :taskIds")
    List<Task> findByIdIn(@Param("taskIds") Collection<Task.TaskId> taskIds);
}
