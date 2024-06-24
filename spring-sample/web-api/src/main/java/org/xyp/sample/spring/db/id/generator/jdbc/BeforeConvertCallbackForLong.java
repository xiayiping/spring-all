package org.xyp.sample.spring.db.id.generator.jdbc;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Service;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;
import org.xyp.sample.spring.db.id.domain.HasId;
import org.xyp.sample.spring.db.id.generator.DatasourceConnectionHolderFactory;
import org.xyp.sample.spring.db.id.generator.specific.IdGeneratorLong;

import javax.sql.DataSource;

@Slf4j
@Service
public class BeforeConvertCallbackForLong<T> implements BeforeConvertCallback<HasId<T, Long>> {

    final IdGeneratorLong idGenerator;
    final DataSource dataSource;
    final JpaProperties jpaProperties;
    final String dialect;

    public BeforeConvertCallbackForLong(
        IdGeneratorLong idGenerator, DataSource dataSource, JpaProperties jpaProperties
    ) {
        this.idGenerator = idGenerator;
        this.dataSource = dataSource;
        this.jpaProperties = jpaProperties;
        this.dialect = jpaProperties.getProperties().get("hibernate.dialect");
    }

    @Override
    public HasId<T, Long> onBeforeConvert(HasId<T, Long> aggregate) {
        val factory = new DatasourceConnectionHolderFactory(dataSource);
        return setIds(aggregate, factory);
    }

    private <TT> HasId<TT, Long> setIds(HasId<TT, Long> aggregate, JdbcConnectionAccessorFactory factory) {
        if (null == aggregate.peekId()) {
            val id = idGenerator.nextId(aggregate.identityGeneratorName(), dialect, factory);
            log.debug("set id for {} to {}", aggregate, id);
            aggregate.refreshId(id);
        }
        val leaves = aggregate.leaves();
        if (null != leaves) {
            leaves.forEach(lv -> setIds(lv, factory));
        }
        return aggregate;
    }

}
