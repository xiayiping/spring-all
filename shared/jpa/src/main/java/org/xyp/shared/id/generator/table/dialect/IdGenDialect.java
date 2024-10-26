package org.xyp.shared.id.generator.table.dialect;

public interface IdGenDialect {


    String getUpdateIdSql();

    String getLastIdSql();

    String getInitIdValueSql();

    String getTableLockSql();

    boolean needUpgradeLockIfIdRecordNotExist();

}
