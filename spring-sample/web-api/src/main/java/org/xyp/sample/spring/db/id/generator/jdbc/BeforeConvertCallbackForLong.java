package org.xyp.sample.spring.db.id.generator.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Service;
import org.xyp.shared.function.Fun;
import org.xyp.shared.function.wrapper.ResultOrError;
import org.xyp.sample.spring.db.id.domain.HasId;
import org.xyp.sample.spring.db.id.generator.DatasourceConnectionHolderFactory;
import org.xyp.shared.db.id.generator.IdGenerator;
import org.xyp.shared.db.id.generator.table.JdbcConnectionAccessorFactory;

import javax.sql.DataSource;

@Slf4j
@Service
public class BeforeConvertCallbackForLong implements BeforeConvertCallback<HasId<Long>> {

    final IdGenerator<Long> idGenerator;
    final DataSource dataSource;
    final JpaProperties jpaProperties;

    public BeforeConvertCallbackForLong(
        IdGenerator<Long> idGenerator, DataSource dataSource, JpaProperties jpaProperties
    ) {
        this.idGenerator = idGenerator;
        this.dataSource = dataSource;
        this.jpaProperties = jpaProperties;
    }

    @Override
    public HasId<Long> onBeforeConvert(HasId<Long> aggregate) {
        return setIds(aggregate, new DatasourceConnectionHolderFactory(dataSource));
    }

    private HasId<Long> setIds(HasId<Long> aggregate, JdbcConnectionAccessorFactory factory) {
        if (null == aggregate.peekId()) {
            ResultOrError.on(() -> idGenerator.nextId(aggregate.identityGeneratorName(), factory))
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
