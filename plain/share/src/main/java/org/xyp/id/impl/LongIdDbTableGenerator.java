package org.xyp.id.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.xyp.id.IdGenerator;
import org.xyp.id.JdbcConnectionAccessorFactory;
import org.xyp.id.dialect.IdGenDialect;
import org.xyp.id.domain.BatchIdResult;
import org.xyp.id.exception.IdGenerationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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

    record StartEnd(
        long startInclude,
        long endInclude,
        int step
    ) {
    }

    @Override
    public List<Long> nextId(String entityName, int fetchSize, JdbcConnectionAccessorFactory factory) {
        return nextId(entityName, fetchSize, DEFAULT_STEP_SIZE, DEFAULT_BATCH_SIZE, factory);
    }

    @Override
    public List<Long> nextId(
        String entityName,
        int fetchSize,
        int defaultStepSize,
        int defaultFetchSize,
        JdbcConnectionAccessorFactory factory
    ) {

        val listOfStartEnd = new ArrayList<StartEnd>();

        idHolder.compute(entityName, (clz, state) -> {
            if (null == state) {
                try (val conn = factory.open()) {
                    val result = updateNextBatchOrCreateNewBatchInDb(entityName, fetchSize, defaultStepSize, defaultFetchSize, conn);
                    val newLast = result.prev() + result.stepSize() * (long) fetchSize;
                    listOfStartEnd.add(new StartEnd(
                        result.prev() + result.stepSize(), newLast, result.stepSize()
                    ));
                    return result.withLast(newLast);
                } catch (SQLException e) {
                    throw new IdGenerationException(e);
                }
            } else {
                val endStateValue = state.prev() + state.stepSize() * (long) fetchSize;
                val gap = endStateValue - state.max();
                val pooledIsEnough = gap <= 0;
                val pooledMax = pooledIsEnough ? endStateValue : state.max();

                val pooledFromTo = new StartEnd(
                    state.prev() + state.stepSize(), pooledMax, state.stepSize()
                );
                listOfStartEnd.add(pooledFromTo);
                log.debug("get batch id for entity {},  from to {}, step_size: {}", entityName, pooledFromTo, state.stepSize());

                if (pooledIsEnough) {
                    return state.withLast(pooledMax);
                } else {
                    val fetchedCount = (pooledFromTo.endInclude >= pooledFromTo.startInclude) ?
                        (pooledFromTo.endInclude - pooledFromTo.startInclude) / pooledFromTo.step + 1
                        : 0;
                    val needFromDbCount = fetchSize - fetchedCount;
                    try (val conn = factory.open()) {
                        val result = updateNextBatchOrCreateNewBatchInDb(entityName, needFromDbCount, state.stepSize(), state.fetchSize(), conn);
                        val newLast = result.prev() + result.stepSize() * needFromDbCount;
                        listOfStartEnd.add(new StartEnd(
                            result.prev() + result.stepSize(), newLast, result.stepSize()
                        ));
                        return result.withLast(newLast);
                    } catch (SQLException e) {
                        throw new IdGenerationException(e);
                    }
                }

            }
        });

        val idList = new ArrayList<Long>();
        for (val startEnd : listOfStartEnd) {
            for (long i = startEnd.startInclude; i <= startEnd.endInclude(); i += startEnd.step()) {
                idList.add(i);
            }
        }
        log.debug("ids for {} : {}", entityName, idList);
        return idList
            ;
    }

    /**
     * Only called when
     *
     * @param entityName       id name
     * @param needFetchSize    how many ids need to be fetched, this function will increase smallest number that can be divided by step_size * default_fetch_size
     * @param defaultStepSize  if entityName not exist in DB, use this as step size, otherwise use step_size stored in db
     * @param defaultFetchSize if entityName not exist in DB, use this as fetch size, otherwise use fetch_size stored in db
     * @return BatchIdResult
     */
    private BatchIdResult updateNextBatchOrCreateNewBatchInDb(
        String entityName,
        long needFetchSize,
        int defaultStepSize,
        long defaultFetchSize,
        final Connection connection
    ) {

        val req = new CalculateBatchFetchSizeReq(needFetchSize, defaultStepSize, defaultFetchSize);
        val calculatedIdIncrease = calculateIdIncrease(req);
        log.debug("calculate fetch size for {}, result {}", req, calculatedIdIncrease);

        try {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            val existingResultInDb = fetchIdBatchFromDB(entityName, connection);
            val retState = ((Supplier<BatchIdResult>) (() -> {
                if (null != existingResultInDb) {
                    val newMax = existingResultInDb.prev() + calculatedIdIncrease;
                    updateIdBatch(entityName, newMax, connection);
                    try {
                        System.out.println("" + Thread.currentThread().getName() + " update db");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new BatchIdResult(
                        existingResultInDb.name(),
                        existingResultInDb.prev(),
                        newMax,
                        existingResultInDb.stepSize(),
                        existingResultInDb.fetchSize()
                    );
                } else {
                    val newState = new BatchIdResult(entityName, 0, calculatedIdIncrease, defaultStepSize, defaultFetchSize);
                    initIdValueToTable(entityName, newState, connection);
                    try {
                        System.out.println("" + Thread.currentThread().getName() + " init db");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return newState;
                }
            })).get();
            connection.commit();
            return retState;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new IdGenerationException(ex);
            }
            throw new IdGenerationException(e);
        }
    }

    record CalculateBatchFetchSizeReq(
        long needFetchCount,
        int defaultStepSize,
        long defaultFetchCount
    ) {
    }

    private long calculateIdIncrease(
        CalculateBatchFetchSizeReq req
    ) {
        long needFetchCount = req.needFetchCount;
        int defaultStepSize = req.defaultStepSize;
        long defaultFetchCount = req.defaultFetchCount;
        val blockSizeInNumber = defaultFetchCount * defaultStepSize;
        val blockRemaining = needFetchCount % defaultFetchCount == 0 ? 0 : 1;
        val needBlock = blockRemaining + needFetchCount / defaultFetchCount;
        return needBlock * blockSizeInNumber;
    }

    private BatchIdResult fetchIdBatchFromDB(String entityName, Connection connection) {
        val getLastIdSql = getLastIdSqlByDialect();
        try (val ps = connection.prepareStatement(getLastIdSql)) {
            log.debug("fetch next id of entity {} {}", entityName, getLastIdSql);
            ps.setString(1, entityName);
            val res = fetchIdBatchFromDB(entityName, ps);
            try {
                System.out.println(Thread.currentThread().getName() + " fetch db");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new IdGenerationException(e);
            }
            return res;
        } catch (SQLException e) {
            throw new IdGenerationException(e);
        }
    }

    private BatchIdResult fetchIdBatchFromDB(String entityName, PreparedStatement preparedStatement) {
        try (val resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                val last = resultSet.getLong(1);
                val step = resultSet.getInt(2);
                val fetchSize = resultSet.getInt(3);
                return BatchIdResult.fromPrev(
                    entityName,
                    last,
                    step,
                    fetchSize
                );
            }
            log.debug("not found entity id item {} in db", entityName);
            return null;
        } catch (SQLException e) {
            throw new IdGenerationException(e);
        }
    }

    private void initIdValueToTable(String entityName, BatchIdResult initBatch, Connection connection) {
        val initIdValueSql = getInitIdValueSqlByDialect();
        try (val ps = connection.prepareStatement(initIdValueSql)) {
            log.debug("init id info for entity {} {}", entityName, initIdValueSql);
            ps.setString(1, entityName);
            ps.setLong(2, (long) initBatch.max());
            ps.setInt(3, initBatch.stepSize());
            ps.setLong(4, initBatch.fetchSize());
            log.debug("init id of entity {}", entityName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IdGenerationException(e);
        }
    }

    private void updateIdBatch(String entityName, long last, Connection connection) {
        final var updateIdSql = getUpdateIdSqlByDialect();
        try (val ps = connection.prepareStatement(updateIdSql)) {
            log.debug("update id for entity {} {} ", entityName, updateIdSql);
            ps.setLong(1, last);
            ps.setString(2, entityName);
            val i = ps.executeUpdate();
            log.debug("update id, updated row count {}", i);
        } catch (SQLException e) {
            throw new IdGenerationException(e);
        }
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
