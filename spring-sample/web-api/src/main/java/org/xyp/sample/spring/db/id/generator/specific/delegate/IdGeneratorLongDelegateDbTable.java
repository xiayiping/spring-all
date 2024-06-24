package org.xyp.sample.spring.db.id.generator.specific.delegate;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.springframework.util.StringUtils;
import org.xyp.exceptions.ValidateException;
import org.xyp.function.Fun;
import org.xyp.function.ValueHolder;
import org.xyp.function.wrapper.WithCloseable;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.sample.spring.db.id.ConnectionHolder;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;
import org.xyp.sample.spring.db.id.dialect.IdGenDialect;
import org.xyp.sample.spring.db.id.dialect.IdGenDialectPostgre;
import org.xyp.sample.spring.db.id.domain.BatchIdResult;
import org.xyp.sample.spring.db.id.generator.IdGenerationException;
import org.xyp.sample.spring.db.id.generator.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class IdGeneratorLongDelegateDbTable implements IdGenerator<Long> {

    final String schemaSql;
    final String table;
    final String entityNameColumn;
    final String prevValueColumn;
    final String stepSizeColumn;
    final String fetchSizeColumn;

    public static final int DEFAULT_BATCH_SIZE = 50;
    public static final int DEFAULT_STEP_SIZE = 1;

    private final ConcurrentHashMap<String, BatchIdResult> idHolder = new ConcurrentHashMap<>();

    private static final String ID_TABLE = "id_table";
    private static final String ENTITY_NAME = "entity_name";
    private static final String PREV_VALUE = "prev_value";
    private static final String STEP_SIZE = "step_size";
    private static final String FETCH_SIZE = "fetch_size";

    private static final IdGenDialect defaultDialect = new IdGenDialect();

    private static final HashMap<String, IdGenDialect> dialectMap = new HashMap<>();

    static {
        dialectMap.put(SQLServerDialect.class.getName(), new IdGenDialect());
        dialectMap.put(PostgreSQLDialect.class.getName(), new IdGenDialectPostgre());
    }

    public IdGeneratorLongDelegateDbTable() {
        this(
            "test",
            ID_TABLE,
            ENTITY_NAME,
            PREV_VALUE,
            STEP_SIZE,
            FETCH_SIZE
        );
    }

    public IdGeneratorLongDelegateDbTable(
        String schema,
        String table,
        String entityNameColumn,
        String prevValueColumn,
        String stepSizeColumn,
        String fetchSizeColumn
    ) {

        schema = StringUtils.hasText(schema) ? schema.trim() : schema;
        this.table = null == table ? ID_TABLE : table.trim();
        this.entityNameColumn = null == entityNameColumn ? ENTITY_NAME : entityNameColumn.trim();
        this.prevValueColumn = null == prevValueColumn ? PREV_VALUE : prevValueColumn.trim();
        this.stepSizeColumn = null == stepSizeColumn ? STEP_SIZE : stepSizeColumn.trim();
        this.fetchSizeColumn = null == fetchSizeColumn ? FETCH_SIZE : fetchSizeColumn.trim();
        validate(schema);
        this.schemaSql = Optional.ofNullable(schema)
            .filter(StringUtils::hasText)
            .map(s -> s + ".")
            .orElse("");
    }

    void validate(String schema) {
        String regex = "[a-zA-Z_]+";
        val pattern = Pattern.compile(regex);
        if (StringUtils.hasText(schema) && !pattern.matcher(schema).matches()) {
            throw new ValidateException("schema name doesn't match [a-zA-Z_], provided value: " + schema);
        }

        if (!pattern.matcher(table).matches()
            || !pattern.matcher(entityNameColumn).matches()
            || !pattern.matcher(prevValueColumn).matches()
            || !pattern.matcher(stepSizeColumn).matches()
            || !pattern.matcher(fetchSizeColumn).matches()
        ) {
            throw new ValidateException("one of table entity prevVal stepSize fetchSize column name invalid , should be [a-zA-Z_], but is "
                + table + " , "
                + entityNameColumn + " , "
                + prevValueColumn + " , "
                + stepSizeColumn + " , "
                + fetchSizeColumn
            );
        }
    }

    @Override
    public Long nextId(String entityName, String dialect, JdbcConnectionAccessorFactory factory) {
        return nextId(entityName, 1, dialect, factory).getFirst();
    }

    @Override
    public List<Long> nextId(String entityName, int fetchSize, String dialect, JdbcConnectionAccessorFactory factory) {

        val cachedState = idHolder.computeIfAbsent(entityName, clz -> fetchOrCreateIdBatchStateFromDB(entityName, dialect, factory));

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
                    .map(ConnectionHolder::connection)
                    .map(connection -> fetchAndUpdateIdInDB(
                        entityName,
                        fetchSize,
                        state,
                        newStartStateHolder,
                        dialect,
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
        String dialect,
        Connection connection
    ) throws SQLException {
        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            int remainingCount = (int) (fetchSize - (currentState.max() - currentState.prev()) / currentState.stepSize());

            val newStartState = fetchIdBatchFromDB(entityName, dialect, connection);
            newStartStateHolder.setValue(newStartState);

            val adder = remainingCount * newStartState.stepSize();
            val remainder = adder % newStartState.fetchSize();
            val multiplier = adder / newStartState.fetchSize();

            val newMax = newStartState.prev()
                + (long) multiplier * newStartState.fetchSize()
                + (remainder > 0 ? newStartState.fetchSize() : 0);
            val newOne = newStartState.withLastAndMax(adder + newStartState.prev(), newMax);
            this.updateIdBatch(entityName, newOne.max(), dialect, connection);
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
    private BatchIdResult fetchOrCreateIdBatchStateFromDB(String entityName, String dialect, JdbcConnectionAccessorFactory factory) {
        return WithCloseable.open(factory::open)
            .map(ConnectionHolder::connection)
            .map(connection -> fetchOrCreateIdBatchStateFromDB(entityName, dialect, connection))
            .closeAndGet(IdGenerationException.class, IdGenerationException::new)
            ;
    }

    private BatchIdResult fetchOrCreateIdBatchStateFromDB(String entityName, String dialect, Connection connection) throws SQLException {
        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            final var holder = fetchIdBatchFromDB(entityName, dialect, connection);

            if (null == holder) {
                val newHolder = new BatchIdResult(entityName, 0L, DEFAULT_STEP_SIZE, DEFAULT_BATCH_SIZE);
                initIdValueToTable(entityName, newHolder, dialect, connection);
                connection.commit();
                return newHolder;
            } else {
                this.updateIdBatch(entityName, holder.max(), dialect, connection);
                connection.commit();
                return holder;
            }
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    private BatchIdResult fetchIdBatchFromDB(String entityName, String dialect, Connection connection) {
        val getLastIdSql = getLastIdSqlByDialect(dialect);
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

    private void initIdValueToTable(String entityName, BatchIdResult initBatch, String dialect, Connection connection) {
        val initIdValueSql = getInitIdValueSqlByDialect(dialect);
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
            .getResult(IdGenerationException.class, IdGenerationException::new)
            .get()
        ;
    }

    private void updateIdBatch(String entityName, long last, String dialect, Connection connection) {
        final var updateIdSql = getUpdateIdSqlByDialect(dialect);
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
            .getResult(IdGenerationException.class, IdGenerationException::new)
            .get()
        ;
    }

    private String getUpdateIdSqlByDialect(String dialect) {
        return dialectMap.getOrDefault(dialect, defaultDialect).getUpdateIdSql(
            this.schemaSql,
            this.table,
            this.entityNameColumn,
            this.prevValueColumn
        );
    }

    private String getLastIdSqlByDialect(String dialect) {
        return dialectMap.getOrDefault(dialect, defaultDialect).getLastIdSql(
            this.schemaSql,
            this.table,
            this.entityNameColumn,
            this.prevValueColumn,
            this.stepSizeColumn,
            this.fetchSizeColumn
        );
    }

    private String getInitIdValueSqlByDialect(String dialect) {
        return dialectMap.getOrDefault(dialect, defaultDialect).initIdValueSql(
            this.schemaSql,
            this.table,
            this.entityNameColumn,
            this.prevValueColumn,
            this.stepSizeColumn,
            this.fetchSizeColumn
        );
    }
}
