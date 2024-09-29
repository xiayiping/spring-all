package org.xyp.spring.datajdbc.domain.repository.jdbc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.xyp.spring.datajdbc.domain.entity.BatchEntity;

@Repository
public interface BatchDao extends
    PagingAndSortingRepository<BatchEntity, Long>,
    ListCrudRepository<BatchEntity, Long> {
}
