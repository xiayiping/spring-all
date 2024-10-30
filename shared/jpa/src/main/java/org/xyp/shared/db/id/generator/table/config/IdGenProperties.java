package org.xyp.shared.db.id.generator.table.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xyp.shared.db.id.generator.table.dialect.DialectType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdGenProperties {
    String schema;
    String table;
    String entityNameColumn;
    String prevValueColumn;
    String stepSizeColumn;
    String fetchSizeColumn;
    DialectType dialect;
    long waitInMilliSecIfCreateIdRecordError = 500;
    boolean fallback;
    /**
     * <br/>refer to another id generator by name, and also
     * <br/>refer to another datasource by name
     */
    String referTo;
}
