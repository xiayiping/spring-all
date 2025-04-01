package a.b.c;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestJdbcTemplate {
    JdbcTemplate jdbcTemplate;

    @Test
    public void testJdbcTemplate() {
        jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.batchUpdate("" , new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {


            }

            @Override
            public int getBatchSize() {
                return 0;
            }
        });
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    return null;
                }
            }
        );
    }
}
