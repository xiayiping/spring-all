package org.xyp.shared.db.datasource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xyp.shared.db.id.generator.table.config.IdGenProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceAllConfig {
    DataSourceProperties properties = new DataSourceProperties();
    JpaProperties jpaProperties = new JpaProperties();
    IdGenProperties idGenerator = new IdGenProperties();
}
