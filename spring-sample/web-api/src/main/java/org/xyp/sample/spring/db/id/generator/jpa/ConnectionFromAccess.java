package org.xyp.sample.spring.db.id.generator.jpa;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.xyp.sample.spring.db.id.ConnectionHolder;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
class ConnectionFromAccess implements /*ConnectionHolder,*/ JdbcConnectionAccessorFactory {
    private final JdbcConnectionAccess access;
    private Connection connection;

    ConnectionFromAccess(JdbcConnectionAccess access) {
        this.access = access;
    }

    @Override
    public void close() throws Exception {
        log.debug("</jdbcConnection action=\"release\" to=\"JdbcConnectionAccess\">");
        access.releaseConnection(connection);
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public ConnectionHolder open() throws SQLException {
        log.debug("<jdbcConnection action=\"obtain\" from=\"JdbcConnectionAccess\">");
        if (null == this.connection)
            this.connection = access.obtainConnection();
        return this;
    }
}
