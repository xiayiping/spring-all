package org.xyp.shared.db.id.generator.table;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcConnectionAccessorFactory {
    Connection open() throws SQLException;
}
