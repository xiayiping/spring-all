package org.xyp.demo.api.cache;

import java.time.LocalDateTime;
import java.util.List;

public record TimeStampedList<V>(
    LocalDateTime time,
    List<V> list) {
}
