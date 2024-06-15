package org.xyp.sample.spring.db.id.jdbc;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Service;
import org.xyp.sample.spring.db.id.entity.IdHolder;

@Slf4j
@Service
public class LongBeforeConvertCallback<T> implements BeforeConvertCallback<IdHolder<Long, T>> {

    @Override
    public IdHolder<Long, T> onBeforeConvert(IdHolder<Long, T> aggregate) {
        val id = System.currentTimeMillis();
        log.debug("set id for {} to {}", aggregate, id);
        return aggregate.withId(id);
    }
}
