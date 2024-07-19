package org.xyp.sample.spring.db.id.generator.jpa;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
class ConnectionFromAccess implements JdbcConnectionAccessorFactory {
    private final JdbcConnectionAccess access;
    private Connection connection;

    ConnectionFromAccess(JdbcConnectionAccess access) {
        this.access = access;
    }

    @Override
    public Connection open() throws SQLException {
        log.debug("<jdbcConnection action=\"obtain\" from=\"JdbcConnectionAccess\">");
        return access.obtainConnection();
    }
}
