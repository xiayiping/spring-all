package org.xyp.shared.db.id.generator;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatasourceConnectionHolderFactory implements JdbcConnectionAccessorFactory {
    private final DataSource dataSource;

    public DatasourceConnectionHolderFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection open() throws SQLException {
        return dataSource.getConnection();
    }
}
