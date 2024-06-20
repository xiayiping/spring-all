package org.xyp.sample.spring.db.id;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.xyp.sample.spring.db.id.domain.BatchIdResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdGeneratorLong implements IdGenerator<Long> {

    private final ConcurrentHashMap<String, BatchIdResult> idHolder = new ConcurrentHashMap<>();

//    private final IncreaseIdDelegate increaseIdDelegate;

    @Override
    public <T> Long nextId(Class<T> entityClass) {
        return nextId(entityClass.getName());
    }

    public Long nextId(String entityName) {
//        idHolder.computeIfAbsent(entityName, increaseIdDelegate::initHolder);
        final List<Long> resultHolder = new ArrayList<>();
        val result = idHolder.compute(entityName, (eName, holder) ->
            calcNextId(eName, holder, 1, resultHolder));
        log.info("nextId of entityName : {} {} ", entityName, result);
        return resultHolder.get(0);
    }

    private BatchIdResult calcNextId(String entityName, final BatchIdResult holder, int count, List<Long> resultHolder) {
        val step = holder.step();
        val map = IntStream.range(1, count + 1)
            .boxed()
            .collect(Collectors.partitioningBy(i -> (long) i * step + holder.prev() <= holder.last()));

        val withInCurrentHolders = LongStream.range(1, map.getOrDefault(Boolean.TRUE, List.of()).size() + 1)
            .map(idx -> holder.prev() + idx * step)
            .boxed().toList();

        resultHolder.addAll(withInCurrentHolders);

        final int needExtendSize = map.getOrDefault(Boolean.FALSE, List.of()).size();
        final long extendedAmount = (long) needExtendSize * step;
        if (extendedAmount > 0) {
//            val newResult = increaseIdDelegate.increaseIdInDb(entityName, extendedAmount);
//            val extendedIds = LongStream.range(1L, needExtendSize + 1L)
//                .boxed()
//                .map(idx -> newResult.prev() + step * idx).toList();
//            resultHolder.addAll(extendedIds);
//            return new BatchIdResult(newResult.prev() + extendedAmount, newResult.last(), newResult.step(), newResult.fetchSize());
        }
        return new BatchIdResult(holder.prev() + (long) step * count, holder.last(), holder.step(), holder.fetchSize());
    }
}
