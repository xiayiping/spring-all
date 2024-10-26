package org.xyp.sample.spring.webapi.domain.task.entity.stock;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.xyp.shared.id.generator.table.hibernate.CustomizedTableIdGenerator;

@Data
@Entity
public class StockEntity {
    @Id
    @CustomizedTableIdGenerator(name = "StockEntity_Id")
    Long id;

    String symbol;
}
