package org.xyp.sample.spring.db.id.generator;

import org.xyp.sample.spring.db.id.ConnectionHolder;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatasourceConnectionHolderFactory implements JdbcConnectionAccessorFactory {
    private final DataSource dataSource;
    private Connection connection;

    public DatasourceConnectionHolderFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }

    @Override
    public ConnectionHolder open() throws SQLException {
        this.connection = dataSource.getConnection();
        return this;
    }

    @Override
    public Connection connection() {
        return this.connection;
    }
}
