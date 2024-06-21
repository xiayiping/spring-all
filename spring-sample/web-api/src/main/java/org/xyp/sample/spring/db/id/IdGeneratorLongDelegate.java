package org.xyp.sample.spring.db.id;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;
import org.xyp.function.Fun;
import org.xyp.function.wrapper.closeable.WithCloseable;
import org.xyp.sample.spring.db.id.domain.BatchIdResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class IdGeneratorLongDelegate {

    final String schema;
    final String table;
    final String entityNameColumn;
    final String lastValueColumn;
    final String stepSizeColumn;
    final String fetchSizeColumn;
    final String updateIdSql;
    final String getLastIdSql;
    final String initIdValueSql;

    public static final int DEFAULT_BATCH_SIZE = 50;

    public IdGeneratorLongDelegate(
        String schema,
        String table,
        String entityNameColumn,
        String lastValueColumn,
        String stepSizeColumn,
        String fetchSizeColumn
    ) {
        this.schema = StringUtils.hasText(schema) ? schema.trim() : schema;
        this.table = null == table ? "id_table" : table.trim();
        this.entityNameColumn = null == entityNameColumn ? "entity_name" : entityNameColumn.trim();
        this.lastValueColumn = null == lastValueColumn ? "last_value" : lastValueColumn.trim();
        this.stepSizeColumn = null == stepSizeColumn ? "step_size" : stepSizeColumn.trim();
        this.fetchSizeColumn = null == fetchSizeColumn ? "fetch_size" : fetchSizeColumn.trim();
        validate();
        val schemaSql = Optional.ofNullable(schema).map(s -> s + ".").orElse("");
        updateIdSql = "update "
            + schemaSql
            + table + " "
            + "set "
            + lastValueColumn + " = ? "
            + "where "
            + entityNameColumn + " = ?";

        getLastIdSql = "select "
            + lastValueColumn + " "
            + stepSizeColumn + " "
            + fetchSizeColumn + " "
            + "from "
            + schemaSql
            + table + " "
            + "with (updlock, holdlock, rowlock) "
            + "where "
            + entityNameColumn + " = ?";

        initIdValueSql = "insert into "
            + schemaSql + table
            + "(" + entityNameColumn + " , "
            + lastValueColumn + " , "
            + stepSizeColumn + " , "
            + fetchSizeColumn + ")"
            + "values (?, ?, ?, ?)";
    }

    void validate() {
        String regex = "[a-zA-Z_]+";
        val pattern = Pattern.compile(regex);
        if (StringUtils.hasText(schema)) {
            if (!pattern.matcher(schema).matches()) {
                throw new RuntimeException();
            }
        }

        if (!pattern.matcher(table).matches()
            || !pattern.matcher(entityNameColumn).matches()
            || !pattern.matcher(lastValueColumn).matches()
            || !pattern.matcher(stepSizeColumn).matches()
            || !pattern.matcher(fetchSizeColumn).matches()
        ) {
            throw new RuntimeException();
        }
    }

    public BatchIdResult increaseIdInDb(String entityName, long remainedGap, Connection connection) {
        BatchIdResult holder = new BatchIdResult(1, 1, 1, 1);//idTableDao.getLastValueFromTable(entityName);
        val currentLast = holder.max();
        val batchIdSize = holder.fetchSize() * holder.step();
        final long last = holder.max()
            + remainedGap
            - remainedGap % batchIdSize
            + (remainedGap % batchIdSize > 0 ? batchIdSize : 0);
        holder = new BatchIdResult(currentLast, last, holder.step(), holder.fetchSize());
//        idTableDao.updateNextValueToTable(entityName, max);
        return holder;
    }

    public BatchIdResult initHolder(String entityName, Connection connection) {

        final var holder = fetchNextIdBatch(entityName, connection);

        if (null == holder) {
//            val step = dbProperties.getDefaultIdIncreaseSize();
//            val newHolder = new BatchIdResult(0L, (long) dbProperties.getDefaultIdGenerateBatchSize() * step, step, dbProperties.getDefaultIdGenerateBatchSize());
//            idTableDao.initIdValueToTable(entityName, newHolder.max(), step, dbProperties.getDefaultIdGenerateBatchSize());
//            return newHolder;
        }
        return holder;
    }

    private BatchIdResult fetchNextIdBatch(String entityName, Connection connection) {

        WithCloseable.open(() -> connection.prepareStatement(getLastIdSql))
            .map(Fun.thenSelf(ps -> log.info("fetch next batch of id {} ", entityName)))
            .map(Fun.thenSelf(ps -> ps.setString(1, entityName)))
        ;

        try (val preparedStatement = connection.prepareStatement(getLastIdSql)) {
            preparedStatement.setString(1, entityName);
            return fetchNextIdBatch(preparedStatement);
        } catch (SQLException e) {
            throw new IdGenerationException(e);
        }
    }

    private static BatchIdResult fetchNextIdBatch(PreparedStatement preparedStatement) throws SQLException {
        return WithCloseable.open(preparedStatement::executeQuery)
            .map(resultSet -> {
                if (resultSet.next()) {
                    val last = resultSet.getLong(1);
                    val step = resultSet.getInt(2);
                    val fetchSize = resultSet.getInt(3);
                    return new BatchIdResult(
                        last,
                        last + (long) step * fetchSize,
                        step,
                        fetchSize
                    );
                }
                return null;
            })
            .closeAndGet()
            .specError(IdGenerationException.class, IdGenerationException::new)
            .get();
    }
}
