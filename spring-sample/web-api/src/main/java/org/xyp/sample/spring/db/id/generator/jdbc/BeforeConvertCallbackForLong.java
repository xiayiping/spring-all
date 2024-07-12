package org.xyp.sample.spring.db.id.generator.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Service;
import org.xyp.function.Fun;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.sample.spring.db.id.JdbcConnectionAccessorFactory;
import org.xyp.sample.spring.db.id.domain.HasId;
import org.xyp.sample.spring.db.id.generator.DatasourceConnectionHolderFactory;
import org.xyp.sample.spring.db.id.generator.specific.IdGeneratorLong;

import javax.sql.DataSource;

@Slf4j
@Service
public class BeforeConvertCallbackForLong implements BeforeConvertCallback<HasId<Long>> {

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
    public HasId<Long> onBeforeConvert(HasId<Long> aggregate) {
        return setIds(aggregate, new DatasourceConnectionHolderFactory(dataSource));
    }

    private HasId<Long> setIds(HasId<Long> aggregate, JdbcConnectionAccessorFactory factory) {
        if (null == aggregate.peekId()) {
            ResultOrError.on(() -> idGenerator.nextId(aggregate.identityGeneratorName(), dialect, factory))
                .map(Fun.updateSelf(id -> log.info("set id for {} to {}", aggregate, id)))
                .map(Fun.updateSelf(aggregate::putGeneratedId))
                .get();
        }
        ResultOrError.on(aggregate::leaves)
            .map(Fun.updateSelf(leaves -> leaves.forEach(lv -> setIds(lv, factory))))
            .get();
        return aggregate;
    }

}
