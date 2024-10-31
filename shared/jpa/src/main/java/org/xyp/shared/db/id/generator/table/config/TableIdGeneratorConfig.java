package org.xyp.shared.db.id.generator.table.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import org.xyp.shared.db.datasource.DataSourcePropertiesGroup;
import org.xyp.shared.db.id.generator.IdGenerator;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialect;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialectH2;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialectMssql;
import org.xyp.shared.db.id.generator.table.dialect.IdGenDialectPostgres;
import org.xyp.shared.db.id.generator.table.impl.LongIdDbTableGenerator;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Configuration
public class TableIdGeneratorConfig {

    public TableIdGeneratorConfig() {
        log.info("id generator config loaded ... ...");
    }

    public IdGenDialect idGenDialect(IdGenProperties idGenProperties) {
        return switch (idGenProperties.getDialect()) {
            case MSSQL -> new IdGenDialectMssql(idGenProperties);
            case H2 -> new IdGenDialectH2(idGenProperties);
            default -> new IdGenDialectPostgres(idGenProperties);
        };
    }

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        val applicationContext = event.getApplicationContext();
        val idGenPropGroup = applicationContext.getBean(DataSourcePropertiesGroup.class);
        val dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
        if (dataSourceMap.size() == 1) {
            fallbackDatasource.set(dataSourceMap.values().iterator().next());
        }
        val referToMap = new HashMap<String, String>();
        idGenPropGroup.getConfigs().forEach((key, comb) -> {
            val value = comb.getIdGenerator();
            if (StringUtils.hasText(value.getReferTo())) {
                referToMap.put(key, value.getReferTo());
            } else {
                val idGen = new LongIdDbTableGenerator(idGenDialect(value), value);
                longIdGeneratorMap.put(
                    key, idGen);
                if (value.fallback) {
                    fallbackIdGenerator.set(idGen);
                    if (fallbackDatasource.get() == null) {
                        fallbackDatasource.set(dataSourceMap.get(key));
                    }
                }
            }
        });

        TableIdGeneratorConfig.datasourceMap.putAll(dataSourceMap);
        referToMap.forEach((key, value) -> {
            longIdGeneratorMap.put(key, getLongIdGenerator(value));
            datasourceMap.put(key, getDataSource(value));
        });
    }

    protected static final AtomicReference<IdGenerator<Long>> fallbackIdGenerator = new AtomicReference<>();
    protected static final AtomicReference<DataSource> fallbackDatasource = new AtomicReference<>();
    protected static final ConcurrentMap<String, DataSource> datasourceMap = new ConcurrentHashMap<>();
    protected static final ConcurrentMap<String, IdGenerator<Long>> longIdGeneratorMap = new ConcurrentHashMap<>();

    public static DataSource getDataSource(String dataSourceName) {
        if (StringUtils.hasText(dataSourceName)) {
            return datasourceMap.get(dataSourceName);
        }
        return fallbackDatasource.getAcquire();
    }

    public static IdGenerator<Long> getLongIdGenerator(String dataSourceName) {
        if (StringUtils.hasText(dataSourceName)) {
            return longIdGeneratorMap.get(dataSourceName);
        }
        return getDefaultIdGenerator();
    }

    public static IdGenerator<Long> getDefaultIdGenerator() {
        return fallbackIdGenerator.getAcquire();
    }

}
