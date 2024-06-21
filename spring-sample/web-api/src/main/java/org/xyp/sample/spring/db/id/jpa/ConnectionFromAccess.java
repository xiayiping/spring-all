package org.xyp.sample.spring.db.id.jpa;

import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.xyp.sample.spring.db.id.ConnectionHolder;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;

import java.sql.Connection;
import java.sql.SQLException;

class ConnectionFromAccess implements ConnectionHolder, JdbcConnectionAccessorFactory {
    private final JdbcConnectionAccess access;
    private Connection connection;

    ConnectionFromAccess(JdbcConnectionAccess access) {
        this.access = access;
    }

    @Override
    public void close() throws Exception {
        access.releaseConnection(connection);
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public ConnectionHolder open() throws SQLException {
        if (null == this.connection)
            this.connection = access.obtainConnection();
        return this;
    }
}
