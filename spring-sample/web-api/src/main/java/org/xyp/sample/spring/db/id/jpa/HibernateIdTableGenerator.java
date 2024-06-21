package org.xyp.sample.spring.db.id.jpa;

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
import org.xyp.sample.spring.db.id.IdGeneratorLong;

import java.util.EnumSet;
import java.util.Properties;

import static org.hibernate.generator.EventType.INSERT;

@Slf4j
public class HibernateIdTableGenerator implements BeforeExecutionGenerator, Configurable {


    transient IdGeneratorLong idGenerator = new IdGeneratorLong();

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
        val dialect = session.getJdbcServices().getDialect();
        log.info("{}", dialect);
        val acc = session.getJdbcConnectionAccess();
        return idGenerator.nextId(owner.getClass(), idClass, dialect.getClass().getName(), new ConnectionFromAccess(acc));
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    private Class<?> idClass = null;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        idClass = type.getReturnedClass();
        log.info("config id generator for id class {}", idClass);
    }
}
