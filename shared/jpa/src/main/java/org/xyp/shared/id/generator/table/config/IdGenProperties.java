package org.xyp.shared.id.generator.table.config;

import org.xyp.shared.id.generator.table.dialect.DialectType;

public interface IdGenProperties {

    String getSchema();

    String getTable();

    String getEntityNameColumn();

    String getPrevValueColumn();

    String getStepSizeColumn();

    String getFetchSizeColumn();

    DialectType getDialect();

}
