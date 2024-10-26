package org.xyp.sample.spring.db.id.generator.jpa;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.id.Configurable;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.xyp.shared.function.wrapper.ResultOrError;
import org.xyp.shared.id.generator.IdGenerator;
import org.xyp.shared.id.generator.table.exception.IdGenerationException;
import org.xyp.sample.spring.webapi.infra.config.JpaDbConfig;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Properties;

import static org.hibernate.generator.EventType.INSERT;

@Slf4j
public class HibernateIdTableGenerator implements BeforeExecutionGenerator, Configurable {

    transient IdGenerator<Long> idGenerator = null;

    public HibernateIdTableGenerator() {
        log.info("create hibernateIdTableGenerator ......");
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (INSERT != eventType || null == owner) {
            return null;
        }
        if (null == idGenerator) {
            idGenerator = JpaDbConfig.SPRING_BEAN;
        }

        val acc = session.getJdbcConnectionAccess();
        val id =
            ResultOrError.on(() -> {
                    if (null != defaultFetchSize && null != defaultStepSize) {
                        return idGenerator.nextId(idName, 1,  defaultStepSize, defaultFetchSize,new ConnectionFromAccess(acc)).getFirst();
                    } else {
                        return idGenerator.nextId(idName, new ConnectionFromAccess(acc));
                    }
                })
                .getOrSpecError(IdGenerationException.class, ee -> new IdGenerationException(idName, ee));

        if (idClass.isAssignableFrom(Long.class)) {
            return idClass.cast(id);
        }
        return ResultOrError.on(() -> {
                val constructor = idClass.getConstructor(id.getClass()); // Long.TYPE for long
                return constructor.newInstance(id);
            })
            .getOrSpecError(IdGenerationException.class, ee -> new IdGenerationException(idName, ee))
            ;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    private static final String BUILD_IN_GENERATOR_NAME = "GENERATOR_NAME";
    public static final String KEY_DEFAULT_FETCH_SIZE = "DEFAULT_FETCH_SIZE";
    public static final String KEY_DEFAULT_STEP_SIZE = "DEFAULT_STEP_SIZE";

    private Class<?> idClass = null;
    private String idName = null;
    private Integer defaultFetchSize;
    private Integer defaultStepSize;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        idClass = type.getReturnedClass();
        idName = params.getProperty(BUILD_IN_GENERATOR_NAME);
        idGenerator = JpaDbConfig.SPRING_BEAN;
        defaultFetchSize = Optional.ofNullable(params.getProperty(KEY_DEFAULT_FETCH_SIZE)).map(Integer::valueOf).orElse(null);
        defaultStepSize = Optional.ofNullable(params.getProperty(KEY_DEFAULT_STEP_SIZE)).map(Integer::valueOf).orElse(null);
        log.info("config id generator for id class {} {}, default fetch size/step {} {}", idClass, idGenerator, defaultFetchSize, defaultStepSize);
    }
}
