package org.xyp.shared.db.datasource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceProperties {
    String driverClassName = null;
    String jdbcUrl = null;
    String poolName = null;
    String username = null;
    String password = null;
    Integer maximumPoolSize = 5;
}
