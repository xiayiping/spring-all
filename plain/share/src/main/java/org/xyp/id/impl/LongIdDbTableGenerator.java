package org.xyp.id.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.xyp.function.Fun;
import org.xyp.function.ValueHolder;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.function.wrapper.WithCloseable;
import org.xyp.id.IdGenerator;
import org.xyp.id.JdbcConnectionAccessorFactory;
import org.xyp.id.dialect.IdGenDialect;
import org.xyp.id.domain.BatchIdResult;
import org.xyp.id.exception.IdGenerationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
public class LongIdDbTableGenerator implements IdGenerator<Long> {

    final IdGenDialect idGenDialect;

    public static final int DEFAULT_BATCH_SIZE = 50;
    public static final int DEFAULT_STEP_SIZE = 1;

    private final ConcurrentHashMap<String, BatchIdResult> idHolder = new ConcurrentHashMap<>();

    public LongIdDbTableGenerator(
        IdGenDialect idGenDialect
    ) {
        this.idGenDialect = idGenDialect;
    }

    @Override
    public Long nextId(String entityName, JdbcConnectionAccessorFactory factory) {
        return nextId(entityName, 1, factory).getFirst();
    }

    @Override
    public List<Long> nextId(String entityName, int fetchSize, JdbcConnectionAccessorFactory factory) {

        val cachedState = idHolder.computeIfAbsent(entityName, clz -> fetchOrCreateIdBatchStateFromDB(entityName, factory));

        ValueHolder<BatchIdResult> currentStateHolder = new ValueHolder<>(null);
        ValueHolder<BatchIdResult> newStartStateHolder = new ValueHolder<>(null);
        val newState = idHolder.computeIfPresent(entityName, (clz, state) -> {
            currentStateHolder.setValue(state);

            val totalLast = state.prev() + (long) fetchSize * state.stepSize();

            if (state.max() >= totalLast) {
                val newLast = cachedState.prev() + (long) cachedState.stepSize() * fetchSize;
                return state.withLast(newLast);
            } else {
                return WithCloseable.open(factory::open)
                    .map(connection -> fetchAndUpdateIdInDB(
                        entityName,
                        fetchSize,
                        state,
                        newStartStateHolder,
                        connection
                    ))
                    .closeAndGet(IdGenerationException.class, IdGenerationException::new)
                    ;
            }
        });
        return Stream.concat(
            currentStateHolder.value() == null ? Stream.empty() : Stream.iterate(
                currentStateHolder.value().prev() + currentStateHolder.value().stepSize(),
                current -> current <= currentStateHolder.value().max(),
                current -> current + currentStateHolder.value().stepSize()
            )
            ,
            newStartStateHolder.value() == null || newState == null ? Stream.empty() : Stream.iterate(
                newStartStateHolder.value().prev() + newStartStateHolder.value().stepSize(),
                current -> current <= newState.prev(),
                current -> current + newStartStateHolder.value().stepSize()
            )
        ).toList()
            ;
    }

    private BatchIdResult fetchAndUpdateIdInDB(
        String entityName,
        int fetchSize,
        BatchIdResult currentState,
        ValueHolder<BatchIdResult> newStartStateHolder,
        Connection connection
    ) throws SQLException {
        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            int remainingCount = (int) (fetchSize - (currentState.max() - currentState.prev()) / currentState.stepSize());

            val newStartState = fetchIdBatchFromDB(entityName, connection);
            newStartStateHolder.setValue(newStartState);

            val adder = remainingCount * newStartState.stepSize();
            val remainder = adder % newStartState.fetchSize();
            val multiplier = adder / newStartState.fetchSize();

            val newMax = newStartState.prev()
                + (long) multiplier * newStartState.fetchSize()
                + (remainder > 0 ? newStartState.fetchSize() : 0);
            val newOne = newStartState.withLastAndMax(adder + newStartState.prev(), newMax);
            this.updateIdBatch(entityName, newOne.max(), connection);
            connection.commit();
            return newOne;
        } catch (Exception exception) {
            connection.rollback();
            throw new IdGenerationException(exception);
        }
    }


    /**
     * run with transaction TRANSACTION_READ_COMMITTED
     *
     * @param entityName entityName
     * @param factory    factory
     * @return BatchIdResult
     */
    private BatchIdResult fetchOrCreateIdBatchStateFromDB(String entityName, JdbcConnectionAccessorFactory factory) {
        return WithCloseable.open(factory::open)
            .map(connection -> fetchOrCreateIdBatchStateFromDB(entityName, connection))
            .closeAndGet(IdGenerationException.class, IdGenerationException::new)
            ;
    }

    private BatchIdResult fetchOrCreateIdBatchStateFromDB(String entityName, Connection connection) throws SQLException {
        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            final var holder = fetchIdBatchFromDB(entityName, connection);

            if (null == holder) {
                val newHolder = new BatchIdResult(entityName, 0L, DEFAULT_STEP_SIZE, DEFAULT_BATCH_SIZE);
                initIdValueToTable(entityName, newHolder, connection);
                connection.commit();
                return newHolder;
            } else {
                this.updateIdBatch(entityName, holder.max(), connection);
                connection.commit();
                return holder;
            }
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    private BatchIdResult fetchIdBatchFromDB(String entityName, Connection connection) {
        val getLastIdSql = getLastIdSqlByDialect();
        return WithCloseable.open(() -> connection.prepareStatement(getLastIdSql))
            .map(Fun.updateSelf(ps -> log.info("fetch next id of entity {} {}", entityName, getLastIdSql)))
            .map(Fun.updateSelf(ps -> ps.setString(1, entityName)))
            .map(ps -> this.fetchIdBatchFromDB(entityName, ps))
            .closeAndGetResult(IdGenerationException.class, IdGenerationException::new)
            .get()
            ;
    }

    private BatchIdResult fetchIdBatchFromDB(String entityName, PreparedStatement preparedStatement) {
        return WithCloseable.open(preparedStatement::executeQuery)
            .map(resultSet -> {
                if (resultSet.next()) {
                    val last = resultSet.getLong(1);
                    val step = resultSet.getInt(2);
                    val fetchSize = resultSet.getInt(3);
                    return new BatchIdResult(
                        entityName,
                        last,
                        step,
                        fetchSize
                    );
                }
                log.debug("not found entity id item in db");
                return null;
            })
            .closeAndGet(IdGenerationException.class, IdGenerationException::new)
            ;
    }

    private void initIdValueToTable(String entityName, BatchIdResult initBatch, Connection connection) {
        val initIdValueSql = getInitIdValueSqlByDialect();
        WithCloseable.open(() -> connection.prepareStatement(initIdValueSql))
            .map(Fun.updateSelf(ps -> log.info("init id info for entity {} {}", entityName, initIdValueSql)))
            .map(Fun.updateSelf(ps -> {
                ps.setString(1, entityName);
                ps.setLong(2, (long) initBatch.stepSize() * initBatch.fetchSize());
                ps.setInt(3, initBatch.stepSize());
                ps.setInt(4, initBatch.fetchSize());
            }))
            .map(Fun.updateSelf(ps -> log.info("init id of entity {}", entityName)))
            .map(Fun.updateSelf(this::initIdValueToTable))
            .closeAndGetResult(IdGenerationException.class, IdGenerationException::new)
            .get()
        ;
    }

    private void initIdValueToTable(PreparedStatement preparedStatement) {
        ResultOrError.on(preparedStatement::executeUpdate)
            .map(Fun.updateSelf(i -> log.info("init id of entity, inserted row count {}", i)))
            .getResultOrSpecError(IdGenerationException.class, IdGenerationException::new)
            .get()
        ;
    }

    private void updateIdBatch(String entityName, long last, Connection connection) {
        final var updateIdSql = getUpdateIdSqlByDialect();
        WithCloseable.open(() -> connection.prepareStatement(updateIdSql))
            .map(Fun.updateSelf(ps -> log.info("update id for entity {} {} ", entityName, updateIdSql)))
            .map(Fun.updateSelf(ps -> {
                ps.setLong(1, last);
                ps.setString(2, entityName);
            }))
            .consume(this::updateIdBatch)
            .closeAndGetResult(IdGenerationException.class, IdGenerationException::new)
            .get()
        ;
    }

    private void updateIdBatch(PreparedStatement preparedStatement) {
        ResultOrError.on(preparedStatement::executeUpdate)
            .map(Fun.updateSelf(i -> log.info("update id, updated row count {}", i)))
            .getResultOrSpecError(IdGenerationException.class, IdGenerationException::new)
            .get()
        ;
    }

    private String getUpdateIdSqlByDialect() {
        return idGenDialect.getUpdateIdSql();
    }

    private String getLastIdSqlByDialect() {
        return idGenDialect.getLastIdSql();
    }

    private String getInitIdValueSqlByDialect() {
        return idGenDialect.getInitIdValueSql();
    }
}
