package org.xyp.id;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcConnectionAccessorFactory {
    Connection open() throws SQLException;
}
