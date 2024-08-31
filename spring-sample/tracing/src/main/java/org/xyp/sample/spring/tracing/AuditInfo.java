package org.xyp.sample.spring.tracing;

import java.util.concurrent.atomic.AtomicInteger;

public record AuditInfo(
    int level,
    int order,
    AtomicInteger childrenSize
) {
    public static AuditInfo ofNewOne() {
        return new AuditInfo(0, 0, new AtomicInteger(0));
    }
    public static AuditInfo ofNewOne(int level, int order) {
        return new AuditInfo(level, order, new AtomicInteger(0));
    }
}
