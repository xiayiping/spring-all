package org.xyp.sample.spring.db.id.domain;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class BatchIdResult {
    final String name;
    final long prev;
    final long max;
    final int stepSize;
    final int fetchSize;

    private BatchIdResult(String name, long prev, long max, int stepSize, int fetchSize) {
        this.name = name;
        this.prev = prev;
        this.max = max;
        this.stepSize = stepSize;
        this.fetchSize = fetchSize;
    }

    public BatchIdResult(String name, long prev, int stepSize, int fetchSize) {
        this(name, prev, prev + (long) stepSize * fetchSize, stepSize, fetchSize);
    }

    public BatchIdResult withLast(long newLast) {
        return new BatchIdResult(name, newLast, max, stepSize, fetchSize);
    }

    public BatchIdResult withLastAndMax(long newLast, long newMax) {
        return new BatchIdResult(name, newLast, newMax, stepSize, fetchSize);
    }

    public long prev() {
        return prev;
    }

    public long max() {
        return max;
    }

    public int stepSize() {
        return stepSize;
    }

    public int fetchSize() {
        return fetchSize;
    }

}
