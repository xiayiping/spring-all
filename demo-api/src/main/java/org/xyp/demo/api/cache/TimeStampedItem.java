package org.xyp.demo.api.cache;

import java.time.LocalDateTime;

public record TimeStampedItem<V>(
    LocalDateTime time,
    V item
) {
}
