package org.xyp.sample.spring.db.id.generator.jpa;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.xyp.id.JdbcConnectionAccessorFactory;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
class ConnectionFromAccess implements JdbcConnectionAccessorFactory {
    private final JdbcConnectionAccess access;

    ConnectionFromAccess(JdbcConnectionAccess access) {
        this.access = access;
    }

    @Override
    public Connection open() throws SQLException {
        log.debug("<jdbcConnection action=\"obtain\" from=\"JdbcConnectionAccess\">");
        Connection connection = access.obtainConnection();
        return connection;
    }
}
