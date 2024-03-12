package org.xyp.demo.api.cache;

import lombok.val;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

abstract public class ListCache<K, V> {

    Map<K, TimeStampedList<V>> cacheMap = new ConcurrentHashMap<>();

    public Optional<List<V>> get(K key,
                                 BiFunction<LocalDateTime, K, List<V>> loader) {
        LocalDateTime now = LocalDateTime.now();
        val tItem = cacheMap.get(key);
        if (needRefresh(now, tItem)) {
            return Optional.ofNullable(cacheMap.compute(key, (k, v) -> {
                if (needRefresh(now, v)) {
                    return new TimeStampedList<>(now, loader.apply(now, k));
                }
                return v;
            })).map(TimeStampedList::list);
        }
        return Optional.ofNullable(tItem).map(TimeStampedList::list);
    }

    public List<V> check(K key,
                         BiFunction<LocalDateTime, K, List<V>> loader,
                         UnaryOperator<List<V>> checker) {

        LocalDateTime now = LocalDateTime.now();
        return cacheMap.compute(key, (k, v) -> {
            if (needRefresh(now, v)) {
                return new TimeStampedList<>(
                    now,
                    checker.apply(loader.apply(now, k)));
            }
            return new TimeStampedList<>(v.time(), checker.apply(v.list()));
        }).list();
    }

    abstract boolean needRefresh(LocalDateTime time, TimeStampedList<V> timeStampedItem);
}
