package org.xyp.sample.spring.db.id.generator.specific;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;
import org.xyp.sample.spring.db.id.generator.IdGenerator;
import org.xyp.sample.spring.db.id.generator.specific.delegate.IdGeneratorLongDelegateDbTable;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdGeneratorLong implements IdGenerator<Long>, ApplicationContextAware {

    private final IdGenerator<Long> delegateDbTable = new IdGeneratorLongDelegateDbTable();

    public static volatile IdGenerator<Long> SPRING_BEAN = null;

    @Override
    public Long nextId(
        String entityName,
        String dialect,
        JdbcConnectionAccessorFactory connectionGetter
    ) {
        return delegateDbTable.nextId(entityName, dialect, connectionGetter);
    }

    @Override
    public List<Long> nextId(
        String entityName,
        int fetchSize,
        String dialect,
        JdbcConnectionAccessorFactory connectionGetter
    ) {
        return delegateDbTable.nextId(entityName, fetchSize, dialect, connectionGetter);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SPRING_BEAN = this;
    }
}
