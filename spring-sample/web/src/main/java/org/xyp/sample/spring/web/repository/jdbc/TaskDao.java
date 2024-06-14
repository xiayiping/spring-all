package org.xyp.sample.spring.web.repository.jdbc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.web.domain.entity.Task;

@Repository
public interface TaskDao extends ListCrudRepository<Task, Long> {
}
