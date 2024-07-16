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
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.sample.spring.db.id.generator.IdGenerationException;
import org.xyp.sample.spring.db.id.generator.IdGenerator;
import org.xyp.sample.spring.db.id.generator.specific.IdGeneratorLong;

import java.util.EnumSet;
import java.util.Properties;

import static org.hibernate.generator.EventType.INSERT;

@Slf4j
public class HibernateIdTableGenerator implements BeforeExecutionGenerator, Configurable {

    transient IdGenerator<Long> idGenerator = null;

    public HibernateIdTableGenerator(
//        GenericGenerator genericGenerator
//        CustomSequence config,
//        Member annotatedMember,
//        CustomIdGeneratorCreationContext context
    ) {
        log.info("create hibernateIdTableGenerator ......");
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (INSERT != eventType || null == owner) {
            return null;
        }
        if (null == idGenerator) {
            idGenerator = IdGeneratorLong.SPRING_BEAN;
        }
        val dialect = session.getJdbcServices().getDialect();

        val acc = session.getJdbcConnectionAccess();
        val id = idGenerator.nextId(idName, dialect.getClass().getName(), new ConnectionFromAccess(acc));

        if (idClass.isAssignableFrom(Long.class)) {
            return idClass.cast(id);
        }
        return ResultOrError.on(() -> {
                val constructor = idClass.getConstructor(id.getClass()); // Long.TYPE for long
                return constructor.newInstance(id);
            })
            .getOrSpecError(IdGenerationException.class, IdGenerationException::new)
            ;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    private Class<?> idClass = null;
    private String idName = null;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        idClass = type.getReturnedClass();
        idName = params.getProperty(IdGenerator.GENERATOR_NAME);
        idGenerator = IdGeneratorLong.SPRING_BEAN;
        log.info("config id generator for id class {}", idClass);
    }
}
