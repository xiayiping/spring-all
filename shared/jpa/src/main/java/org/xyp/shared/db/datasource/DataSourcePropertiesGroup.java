package org.xyp.shared.db.datasource;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static org.xyp.shared.db.DatabaseConfig.DATASOURCE_PROPERTIES_CONFIG_PREFIX;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = DATASOURCE_PROPERTIES_CONFIG_PREFIX)
public class DataSourcePropertiesGroup {
    Map<String, DataSourceAllConfig> configs = new HashMap<>();
}
