package org.xyp.id.dialect;

public interface IdGenProperties {

    String getSchema();

    String getTable();

    String getEntityNameColumn();

    String getPrevValueColumn();

    String getStepSizeColumn();

    String getFetchSizeColumn();
}
