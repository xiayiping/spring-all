package org.xyp.shared.id.generator.table.dialect;

public interface IdGenProperties {

    String getSchema();

    String getTable();

    String getEntityNameColumn();

    String getPrevValueColumn();

    String getStepSizeColumn();

    String getFetchSizeColumn();

    DialectType getDialect();

    long getWaitInMilliSecIfCreateIdRecordError();
}
