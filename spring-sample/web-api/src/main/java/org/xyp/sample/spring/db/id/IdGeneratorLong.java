package org.xyp.sample.spring.db.id;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.xyp.function.wrapper.exceptional.ResultOrError;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdGeneratorLong implements IdGenerator<Long> {

    private final IdGeneratorLongDelegateDbTable delegateDbTable = new IdGeneratorLongDelegateDbTable();

    @Override
    public <T, W> W nextId(
        Class<T> entityClass,
        Class<W> idWrapperClass,
        String dialect,
        JdbcConnectionAccessorFactory connectionGetter
    ) {
        return nextId(entityClass, idWrapperClass, 1, dialect, connectionGetter).stream().findFirst().orElseThrow(() -> new IdGenerationException(
            "not able to generate id for " + entityClass.getName()));
    }

    @Override
    public <T, W> List<W> nextId(
        Class<T> entityClass,
        Class<W> idWrapperClass,
        int fetchSize,
        String dialect,
        JdbcConnectionAccessorFactory connectionGetter
    ) {
        val ids = delegateDbTable.nextId(entityClass, fetchSize, dialect, connectionGetter);
        if (idWrapperClass.isAssignableFrom(Long.class)) {
            return ids.stream().map(idWrapperClass::cast).toList();
        }
        return ResultOrError.on(() -> {
                val constructor = idWrapperClass.getConstructor(Long.TYPE);
                return ids.stream()
                    .map(id -> ResultOrError.on(() -> constructor.newInstance(id)).get())
                    .toList();
            }).specError(IdGenerationException.class, IdGenerationException::new)
            .get();

    }

}
