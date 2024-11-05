package org.xyp.shared.db.id.generator.table.config;

import org.xyp.shared.db.id.generator.IdGenerator;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialect;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialectH2;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialectMssql;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialectPostgres;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TableIdGeneratorConfig {

    protected static final AtomicReference<IdGenerator<Long>> fallbackIdGenerator = new AtomicReference<>();
    protected static final AtomicReference<DataSource> fallbackDatasource = new AtomicReference<>();
    protected static final ConcurrentMap<String, DataSource> datasourceMap = new ConcurrentHashMap<>();
    protected static final ConcurrentMap<String, IdGenerator<Long>> longIdGeneratorMap = new ConcurrentHashMap<>();

    public static Function<String, DataSource> getDataSource;
    public static Function<String, IdGenerator<Long>> getLongIdGenerator;
    public static Supplier<IdGenerator<Long>> getDefaultIdGenerator;

    public IdGenDialect idGenDialect(IdGenProperties idGenProperties) {
        return switch (idGenProperties.getDialect()) {
            case MSSQL -> new IdGenDialectMssql(idGenProperties);
            case H2 -> new IdGenDialectH2(idGenProperties);
            default -> new IdGenDialectPostgres(idGenProperties);
        };
    }

}
