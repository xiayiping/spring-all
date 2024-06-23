package org.xyp.sample.spring.db.id.dialect;

public class IdGenDialectPostgre extends IdGenDialect {

    public String getUpdateIdSql(
        String schema,
        String table,
        String entityNameColumn,
        String prevValueColumn
    ) {
        return "update "
            + schema
            + table + " "
            + "set "
            + prevValueColumn + " = ? "
            + "where "
            + entityNameColumn + " = ?";
    }

    public String getLastIdSql(
        String schema,
        String table,
        String entityNameColumn,
        String prevValueColumn,
        String stepSizeColumn,
        String fetchSizeColumn
    ) {
        return "select "
            + prevValueColumn + ", "
            + stepSizeColumn + ", "
            + fetchSizeColumn + " "
            + "from "
            + schema
            + table + " "
            + "where "
            + entityNameColumn + " = ? "
            + "for update";
    }

    public String initIdValueSql(
        String schema,
        String table,
        String entityNameColumn,
        String prevValueColumn,
        String stepSizeColumn,
        String fetchSizeColumn
    ) {
        return "insert into "
            + schema + table
            + "("
            + entityNameColumn + " , "
            + prevValueColumn + " , "
            + stepSizeColumn + " , "
            + fetchSizeColumn + ") "
            + "values (?, ?, ?, ?)";
    }

}
