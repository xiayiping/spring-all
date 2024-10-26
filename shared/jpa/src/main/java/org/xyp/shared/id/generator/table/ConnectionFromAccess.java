package org.xyp.shared.id.generator.table;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class ConnectionFromAccess implements JdbcConnectionAccessorFactory {
    private final JdbcConnectionAccess access;

    public ConnectionFromAccess(JdbcConnectionAccess access) {
        this.access = access;
    }

    @Override
    public Connection open() throws SQLException {
        log.debug("<jdbcConnection action=\"obtain\" from=\"JdbcConnectionAccess\">");
        Connection connection = access.obtainConnection();
        return connection;
    }
}
