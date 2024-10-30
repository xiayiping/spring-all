package org.xyp.shared.db;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.xyp.shared.db.datasource.DataSourcePropertiesGroup;

@Configuration
@EnableConfigurationProperties(DataSourcePropertiesGroup.class)
public class DatabaseConfig {
    public static final String DATASOURCE_PROPERTIES_CONFIG_PREFIX = "org.xyp.shared.datasource";
    public static final String DATASOURCE_PROPERTIES_PROPERTIES_PREFIX = "org.xyp.shared.datasource.configs";
}
