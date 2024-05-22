package org.xyp.demo.api.cache;

import lombok.val;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface ItemCacheInterface2<K1, K2, K3, V>
    extends ItemCacheInterface1<K2, K3, V> {

    default Map<K3, TimeStampedItem<V>> getLeafMap(
        Map<K1, Map<K2, Map<K3, TimeStampedItem<V>>>> theMap,
        K1 key1, K2 key2
    ) {
        return getLeafMap(theMap.computeIfAbsent(key1, k -> new ConcurrentHashMap<>()),
            key2);
    }

    default TimeStampedItem<V> getOrLoadIfAbsent1(
        Map<K1, Map<K2, Map<K3, TimeStampedItem<V>>>> theMap,
        LocalDateTime now, K1 key1, K2 key2, K3 key3,
        Function<K1, Function<K2, Function<K3, Function<LocalDateTime, V>>>> loader
    ) {
        val map1 = getLeafMap(theMap, key1, key2);
        return getOrLoadIfAbsent(getLeafMap(theMap, key1, key2),
            now, key3, loader.apply(key1).apply(key2));
    }
}
