package org.xyp.sample.spring.webapi.domain.task.repository.jpa2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.task.entity.stock.StockEntity;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long>, JpaSpecificationExecutor<StockEntity> {
}
