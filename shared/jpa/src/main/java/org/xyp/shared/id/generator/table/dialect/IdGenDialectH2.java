package org.xyp.shared.id.generator.table.dialect;

import org.apache.commons.lang3.StringUtils;
import org.xyp.shared.id.generator.table.config.IdGenProperties;

import java.util.Optional;

public class IdGenDialectH2 implements IdGenDialect {

    final String updateIdSql;
    final String lastIdSql;
    final String initIdValueSql;
    final String tableLockSql;

    public IdGenDialectH2(
        IdGenProperties idGenProperties
    ) {
        final String schemaPrefix = Optional.ofNullable(idGenProperties.getSchema())
            .filter(s -> !StringUtils.isEmpty(s))
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
            + idGenProperties.getTable() + " id_t "
            + "where "
            + idGenProperties.getEntityNameColumn() + " = ? "
            + "for update ";

        initIdValueSql = "insert into "
            + schemaPrefix + idGenProperties.getTable()
            + "("
            + idGenProperties.getEntityNameColumn() + " , "
            + idGenProperties.getPrevValueColumn() + " , "
            + idGenProperties.getStepSizeColumn() + " , "
            + idGenProperties.getFetchSizeColumn() + ") "
            + "values (?, ?, ?, ?)";

//        tableLockSql = "UPDATE "
//            + schemaPrefix + idGenProperties.getTable()
//            + " set "
//            + idGenProperties.getEntityNameColumn()
//            + "="
//            + idGenProperties.getEntityNameColumn()
//            + " where 1=0"
//        ;

        tableLockSql = "select * from "
            + schemaPrefix + idGenProperties.getTable()
            + " where "
            + idGenProperties.getEntityNameColumn() + " = 'for_lock' "
            + " for update ";

//        tableLockSql = "insert into "
//            + schemaPrefix + idGenProperties.getTable()
//            + "("
//            + idGenProperties.getEntityNameColumn() + " , "
//            + idGenProperties.getPrevValueColumn() + " , "
//            + idGenProperties.getStepSizeColumn() + " , "
//            + idGenProperties.getFetchSizeColumn() + ") "
//            + "values ('for_lock', 1, 1, 1)";
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
    public boolean needUpgradeLockIfIdRecordNotExist() {
        return true;
    }

    @Override
    public String getTableLockSql() {
        return tableLockSql;
    }
}
