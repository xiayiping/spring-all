package org.xyp.id.dialect;

public interface IdGenDialect {


    String getUpdateIdSql();

    String getLastIdSql();

    String getInitIdValueSql();


}
