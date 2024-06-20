package org.xyp.sample.spring.db.id;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xyp.sample.spring.db.id.domain.BatchIdResult;

import java.sql.Connection;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
public class IdGeneratorLongDelegate {

    final String schema;
    final String table;
    final String entityNameColumn;
    final String lastValueColumn;
    final String stepColumn;
    final String fetchSizeColumn;
    final String updateIdSql;
    final String getLastIdSql;
    final String initIdValueSql;

    public IdGeneratorLongDelegate(
        String schema,
        String table,
        String entityNameColumn,
        String lastValueColumn,
        String stepColumn,
        String fetchSizeColumn
    ) {
        this.schema = StringUtils.hasText(schema) ? schema.trim() : schema;
        this.table = null == table ? "id_table" : table.trim();
        this.entityNameColumn = null == entityNameColumn ? "entity_name" : entityNameColumn.trim();
        this.lastValueColumn = null == lastValueColumn ? "last_value" : lastValueColumn.trim();
        this.stepColumn = null == stepColumn ? "step_size" : stepColumn.trim();
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
            + stepColumn + " "
            + fetchSizeColumn + " "
            + "from "
            + schemaSql
            + table + " "
            + "with (updlock,holdlock,rowlock) "
            + "where "
            + entityNameColumn + " = ?";

        initIdValueSql = "insert into "
            + schemaSql + table
            + "(" + entityNameColumn + " , "
            + lastValueColumn + " , "
            + stepColumn + " , "
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
            || !pattern.matcher(stepColumn).matches()
            || !pattern.matcher(fetchSizeColumn).matches()
        ) {

            throw new RuntimeException();
        }
    }

    /*
        update KYC.IdTable
        set lastVal=#{lastVal}
        where entityName = #{entityName}
     */
    public BatchIdResult increaseIdInDb(String entityName, long remainedGap, Connection connection) {
        BatchIdResult holder = new BatchIdResult(1,1,1,1);//idTableDao.getLastValueFromTable(entityName);
        val currentLast = holder.last();
        val batchIdSize = holder.fetchSize() * holder.step();
        final long last = holder.last()
            + remainedGap
            - remainedGap % batchIdSize
            + (remainedGap % batchIdSize > 0 ? batchIdSize : 0);
        holder = new BatchIdResult(currentLast, last, holder.step(), holder.fetchSize());
//        idTableDao.updateNextValueToTable(entityName, last);
        return holder;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = REQUIRES_NEW)
    public BatchIdResult initHolder(String entityName) {
        val holder = new BatchIdResult(1,1,1,1);//idTableDao.getLastValueFromTable(entityName);
        if (null == holder) {
//            val step = dbProperties.getDefaultIdIncreaseSize();
//            val newHolder = new BatchIdResult(0L, (long) dbProperties.getDefaultIdGenerateBatchSize() * step, step, dbProperties.getDefaultIdGenerateBatchSize());
//            idTableDao.initIdValueToTable(entityName, newHolder.last(), step, dbProperties.getDefaultIdGenerateBatchSize());
//            return newHolder;
        }
        return holder;
    }
}
