package org.xyp.shared.id.generator.table.hibernate;

import lombok.Data;
import lombok.val;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.xyp.shared.function.wrapper.ResultOrError;
import org.xyp.shared.id.generator.IdGenerator;
import org.xyp.shared.id.generator.table.ConnectionFromAccess;
import org.xyp.shared.id.generator.table.exception.IdGenerationException;

import java.util.EnumSet;

import static org.hibernate.generator.EventType.INSERT;

@Data
public class DefaultBeforeExecutionGeneratorImpl implements BeforeExecutionGenerator {

    transient IdGenerator<Long> idGenerator = null;
    volatile String name;
    volatile int defaultFetchSize = 50;
    volatile int defaultStepSize = 1;
    volatile Class<?> idClass;

    public DefaultBeforeExecutionGeneratorImpl(
        String name,
        int defaultFetchSize,
        int defaultStepSize,
        Class<?> idClass
    ) {
        this.name = name;
        this.defaultFetchSize = defaultFetchSize;
        this.defaultStepSize = defaultStepSize;
        this.idClass = idClass;
    }

    public DefaultBeforeExecutionGeneratorImpl(
        String name,
        Class<?> idClass
    ) {
        this.name = name;
        this.idClass = idClass;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (INSERT != eventType || null == owner) {
            return null;
        }
        if (null == idGenerator) {
            idGenerator = HibernateIdTableGenerator.SPRING_BEAN;
        }

        val acc = session.getJdbcConnectionAccess();
        val id =
            ResultOrError.on(() -> {
                    if (0 < defaultFetchSize && 0 < defaultStepSize) {
                        return idGenerator.nextId(
                            name,
                            1, defaultStepSize, defaultFetchSize,
                            new ConnectionFromAccess(acc)
                        ).getFirst();
                    } else {
                        return idGenerator.nextId(name, new ConnectionFromAccess(acc));
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
