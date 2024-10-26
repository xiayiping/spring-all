package org.xyp.shared.id.generator.table.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.xyp.shared.id.generator.table.dialect.DialectType;

@Data
@ConfigurationProperties(prefix = "org.xyp.shared.id-gen")
public class IdGenPropertiesImpl implements IdGenProperties {
    String schema;
    String table;
    String entityNameColumn;
    String prevValueColumn;
    String stepSizeColumn;
    String fetchSizeColumn;
    DialectType dialect;
    long waitInMilliSecIfCreateIdRecordError = 500;
}
