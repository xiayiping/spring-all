package org.xyp.shared.db.id.generator.table.dialect;

import org.xyp.shared.utils.StringUtils;
import org.xyp.shared.db.id.generator.table.config.IdGenProperties;

import java.util.Optional;

public class IdGenDialectMssql implements IdGenDialect {

    final String updateIdSql;
    final String lastIdSql;
    final String initIdValueSql;

    public IdGenDialectMssql(
        IdGenProperties idGenProperties
    ) {
        final String schemaPrefix = Optional.ofNullable(idGenProperties.getSchema())
            .filter(StringUtils::hasText)
            .map(s -> s + ".")
            .orElse("");
        updateIdSql = "update "
            + schemaPrefix
            + idGenProperties.getTable() + " "
            + "set "
            + idGenProperties.getPrevValueColumn() + " = ? "
            + "where "
            + idGenProperties.getEntityNameColumn() + " = ?";
        lastIdSql = "select "
            + idGenProperties.getPrevValueColumn() + ", "
            + idGenProperties.getStepSizeColumn() + ", "
            + idGenProperties.getFetchSizeColumn() + " "
            + "from "
            + schemaPrefix
            + idGenProperties.getTable() + " "
            + "with (updlock,holdlock,rowlock) "
            + "where "
            + idGenProperties.getEntityNameColumn() + " = ? "
            ;
        initIdValueSql = "insert into "
            + schemaPrefix + idGenProperties.getTable()
            + "("
            + idGenProperties.getEntityNameColumn() + " , "
            + idGenProperties.getPrevValueColumn() + " , "
            + idGenProperties.getStepSizeColumn() + " , "
            + idGenProperties.getFetchSizeColumn() + ") "
            + "values (?, ?, ?, ?)";
    }

    @Override
    public String getUpdateIdSql() {
        return updateIdSql;
    }

    @Override
    public String getLastIdSql() {
        return lastIdSql;
    }

    @Override
    public String getInitIdValueSql() {
        return initIdValueSql;
    }

    @Override
    public String getTableLockSql() {
        throw new IllegalStateException("mssql not need table lock");
    }

    @Override
    public boolean needUpgradeLockIfIdRecordNotExist() {
        return false;
    }

}
