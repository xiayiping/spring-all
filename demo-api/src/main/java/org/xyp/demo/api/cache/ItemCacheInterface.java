package org.xyp.demo.api.cache;

import lombok.val;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

public interface ItemCacheInterface<K, V> {
    boolean needRefresh(LocalDateTime time, TimeStampedItem<V> timeStampedItem);

    default Map<K, TimeStampedItem<V>> getLeafMap(
        Map<K, TimeStampedItem<V>> theMap
    ) {
        return theMap;
    }

    default TimeStampedItem<V> getOrLoadIfAbsent(
        Map<K, TimeStampedItem<V>> tailMap,
        LocalDateTime now, K key,
        Function<K, Function<LocalDateTime, V>> loader
    ) {
        val tItem = tailMap.get(key);
        if (needRefresh(now, tItem)) {
            return tailMap.compute(key, (k, v) -> {
                if (needRefresh(now, v)) {
                    return new TimeStampedItem<>(
                        now,
                        loader.apply(key).apply(now));
                }
                return v;
            });
        }
        return tItem;
    }
}
