package org.xyp.sample.spring.db.id;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionAccessorFactory extends AutoCloseable {
    Connection open() throws SQLException;
}
