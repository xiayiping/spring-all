package org.xyp.sample.spring.db.id.jpa;

import lombok.val;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.xyp.function.Fun;
import org.xyp.function.wrapper.closeable.WithCloseable;
import org.xyp.sample.spring.db.id.IdGenerationException;

import java.sql.Connection;
import java.util.EnumSet;

import static org.hibernate.generator.EventType.INSERT;

public class HibernateIdTableGenerator implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (INSERT != eventType || null == owner) {
            return null;
        }
        val acc = session.getJdbcConnectionAccess();
        try {
            val conn = acc.obtainConnection();
            acc.releaseConnection(conn);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    private void id(JdbcConnectionAccess connectionAccess) {
        WithCloseable.open(connectionAccess::obtainConnection)
            .map(Fun.selfMap(c -> c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED)))
            .map(Fun.selfMap(c -> c.setAutoCommit(false)))
            .close()
            .throwIfError(ex -> Fun.convertRte(ex, IdGenerationException.class, IdGenerationException::new));
    }

    private void id(Connection connection) {

    }
}
