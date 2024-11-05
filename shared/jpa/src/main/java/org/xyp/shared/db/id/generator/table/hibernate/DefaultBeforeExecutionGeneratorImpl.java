package org.xyp.shared.db.id.generator.table.hibernate;

import lombok.Data;
import lombok.val;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.xyp.shared.function.wrapper.ResultOrError;
import org.xyp.shared.db.id.generator.DatasourceConnectionHolderFactory;
import org.xyp.shared.db.id.generator.table.config.TableIdGeneratorConfig;
import org.xyp.shared.db.id.generator.table.exception.IdGenerationException;

import java.util.EnumSet;

import static org.hibernate.generator.EventType.INSERT;

@Data
public class DefaultBeforeExecutionGeneratorImpl implements BeforeExecutionGenerator {

    String name;
    String datasource;
    int defaultFetchSize = 50;
    int defaultStepSize = 1;
    Class<?> idClass;

    public DefaultBeforeExecutionGeneratorImpl(
        String name,
        String datasource,
        int defaultFetchSize,
        int defaultStepSize,
        Class<?> idClass
    ) {
        this.name = name;
        this.datasource = datasource;
        this.defaultFetchSize = defaultFetchSize;
        this.defaultStepSize = defaultStepSize;
        this.idClass = idClass;
    }

    public DefaultBeforeExecutionGeneratorImpl(
        String name,
        String datasource,
        Class<?> idClass
    ) {
        this.name = name;
        this.datasource = datasource;
        this.idClass = idClass;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (INSERT != eventType || null == owner) {
            return null;
        }

        val idGenerator = TableIdGeneratorConfig.getLongIdGenerator.apply(this.datasource);
        val dataSource = TableIdGeneratorConfig.getDataSource.apply(this.datasource);
        val id = ResultOrError.on(() -> {
                    if (0 < defaultFetchSize && 0 < defaultStepSize) {
                        return idGenerator.nextId(
                            name,
                            1, defaultStepSize, defaultFetchSize,
                            new DatasourceConnectionHolderFactory(dataSource)
                        ).getFirst();
                    } else {
                        return idGenerator.nextId(name, new DatasourceConnectionHolderFactory(dataSource));
                    }
                })
                .getOrSpecError(IdGenerationException.class, ee -> new IdGenerationException(name, ee));

        if (idClass.isAssignableFrom(Long.class)) {
            return idClass.cast(id);
        }
        return ResultOrError.on(() -> {
                val constructor = idClass.getConstructor(id.getClass()); // Long.TYPE for long
                return constructor.newInstance(id);
            })
            .getOrSpecError(IdGenerationException.class, ee -> new IdGenerationException(name, ee))
            ;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}
