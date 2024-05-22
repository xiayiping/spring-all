package org.xyp.demo.api.cache;

import lombok.val;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class ItemCache1<K1, K2, V> {
    Map<K1, Map<K2, TimeStampedItem<V>>> cacheMap =
        new ConcurrentHashMap<>();


    public TimeStampedItem<V> get(
        K1 key1, K2 key2,
        Function<LocalDateTime, Function<K1, Function<K2, V>>> loader
    ) {
        LocalDateTime now = LocalDateTime.now();
        return getOrLoadIfAbsent1(cacheMap, now, key1, key2, loader);
    }

    public V check(
        K1 key1, K2 key2,
        Function<LocalDateTime, Function<K1, Function<K2, V>>> loader,
        UnaryOperator<V> checker
    ) {

        LocalDateTime now = LocalDateTime.now();
        return getLeafMap(cacheMap, key1).compute(key2, (k2, v) -> {
            if (needRefresh(now, v)) {
                return new TimeStampedItem<>(
                    now,
                    checker.apply(loader.apply(now).apply(key1).apply(k2)));
            }
            return new TimeStampedItem<>(v.time(), checker.apply(v.item()));
        }).item();
    }

    private Map<K2, TimeStampedItem<V>> getLeafMap(
        Map<K1, Map<K2, TimeStampedItem<V>>> theMap, K1 key1
    ) {
        return theMap.computeIfAbsent(key1, k -> new ConcurrentHashMap<>());
    }

    private TimeStampedItem<V> getOrLoadIfAbsent1(
        Map<K1, Map<K2, TimeStampedItem<V>>> theMap,
        LocalDateTime now, K1 key1, K2 key2,
        Function<LocalDateTime, Function<K1, Function<K2, V>>> loader
    ) {
        val map1 = getLeafMap(theMap, key1);
        return getOrLoadIfAbsent(map1, now, key1, key2, loader);
    }

    private TimeStampedItem<V> getOrLoadIfAbsent(
        Map<K2, TimeStampedItem<V>> tailMap,
        LocalDateTime now, K1 key1, K2 key2,
        Function<LocalDateTime, Function<K1, Function<K2, V>>> loader
    ) {
        val tItem = tailMap.get(key2);
        if (needRefresh(now, tItem)) {
            return tailMap.compute(key2, (k2, v) -> {
                if (needRefresh(now, v)) {
                    return new TimeStampedItem<>(
                        now,
                        loader.apply(now).apply(key1).apply(k2));
                }
                return v;
            });
        }
        return tItem;
    }

    abstract boolean needRefresh(LocalDateTime time, TimeStampedItem<V> timeStampedItem);
}
