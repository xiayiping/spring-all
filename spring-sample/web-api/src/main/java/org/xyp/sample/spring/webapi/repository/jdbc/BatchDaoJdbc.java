package org.xyp.sample.spring.webapi.repository.jdbc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.jdbc.Batch;

@Repository
public interface BatchDaoJdbc extends ListCrudRepository<Batch, Long> {
}
