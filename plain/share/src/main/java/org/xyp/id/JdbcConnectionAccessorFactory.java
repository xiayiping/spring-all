package org.xyp.id;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionAccessorFactory {
    Connection open() throws SQLException;
}
