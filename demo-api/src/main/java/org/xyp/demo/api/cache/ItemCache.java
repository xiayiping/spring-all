package org.xyp.demo.api.cache;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class ItemCache<K, V> implements ItemCacheInterface<K, V> {
    Map<K, TimeStampedItem<V>> cacheMap = new ConcurrentHashMap<>();

    public Optional<V> get(K key,
                           Function<K, Function<LocalDateTime, V>> loader) {
        LocalDateTime now = LocalDateTime.now();
        return Optional.ofNullable(getOrLoadIfAbsent(cacheMap, now, key, loader))
            .map(TimeStampedItem::item);
    }

    public V check(K key,
                   BiFunction<LocalDateTime, K, V> loader,
                   UnaryOperator<V> checker) {

        LocalDateTime now = LocalDateTime.now();
        return cacheMap.compute(key, (k, v) -> {
            if (needRefresh(now, v)) {
                return new TimeStampedItem<V>(
                    now,
                    checker.apply(loader.apply(now, k)));
            }
            return new TimeStampedItem<>(v.time(), checker.apply(v.item()));
        }).item();
    }
}
