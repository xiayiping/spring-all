package org.xyp.demo.api.cache;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface ItemCacheInterface1<K1, K2, V>
    extends ItemCacheInterface<K2, V> {

    default Map<K2, TimeStampedItem<V>> getLeafMap(
        Map<K1, Map<K2, TimeStampedItem<V>>> theMap, K1 key1
    ) {
        return getLeafMap(theMap.computeIfAbsent(key1, k -> new ConcurrentHashMap<>()));
    }

    default TimeStampedItem<V> getOrLoadIfAbsent1(
        Map<K1, Map<K2, TimeStampedItem<V>>> theMap,
        LocalDateTime now, K1 key1, K2 key2,
        Function<K1, Function<K2, Function<LocalDateTime, V>>> loader
    ) {
        return getOrLoadIfAbsent(getLeafMap(theMap, key1),
            now, key2, loader.apply(key1));
    }
}
