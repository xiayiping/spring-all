package org.xyp.sample.spring.webapi.domain.shared.service.transactional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Service
public interface CommonTransactionalService {

    @Transactional(rollbackFor = Exception.class)
    <T, I> T updateEntity(
        I id,
        Function<I, Optional<T>> fetchFunction,
        Consumer<T> updateCommand,
        UnaryOperator<T> saveFunction
    );

    @Transactional(rollbackFor = Exception.class)
    <T, I> List<T> updateEntities(
        Function<T, I> idGetter,
        Function<Iterable<I>, List<T>> fetchFunction,
        Map<I, Consumer<T>> updateCommandMap,
        Function<Iterable<T>, List<T>> saveFunction
    );
}
