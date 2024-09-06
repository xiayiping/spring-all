package org.xyp.sample.spring.webapi.service.transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.xyp.function.Fun;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionalService {

    @Transactional(rollbackFor = Exception.class)
    public <T, I> T updateEntity(
        I id,
        Function<I, Optional<T>> fetchFunction,
        Consumer<T> updateCommand,
        UnaryOperator<T> saveFunction
    ) {
        return fetchFunction.apply(id)
            .map(Fun.consumeSelf(updateCommand))
            .map(saveFunction)
            .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public <T, I> List<T> updateEntities(
        Function<T, I> idGetter,
        Function<Iterable<I>, List<T>> fetchFunction,
        Map<I, Consumer<T>> updateCommandMap,
        Function<Iterable<T>, List<T>> saveFunction
    ) {
        final var entities = fetchFunction.apply(updateCommandMap.keySet());
        if (CollectionUtils.isEmpty(entities)) {
            return entities;
        }
        final var updated = entities.stream()
            .map(entity ->
                Optional.ofNullable(updateCommandMap.get(idGetter.apply(entity)))
                    .map(cmd -> {
                        cmd.accept(entity);
                        return entity;
                    })
                    .orElse(null)
            )
            .filter(Objects::nonNull);

        return saveFunction.apply(updated.toList());
    }
}
