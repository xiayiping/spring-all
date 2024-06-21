package org.xyp.sample.spring.db.id;

import java.sql.Connection;

public interface ConnectionHolder extends AutoCloseable {
    Connection connection();
}
