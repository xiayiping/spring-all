package org.xyp.sample.spring.db.id;

import java.sql.SQLException;

public interface JdbcConnectionAccessorFactory {
    ConnectionHolder open() throws SQLException;
}
