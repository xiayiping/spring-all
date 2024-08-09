package org.xyp.id.domain;


public record BatchIdResult(
    String name,
    long prev,
    long max,
    int stepSize,
    int fetchSize
) {

    public BatchIdResult(String name, long prev, int stepSize, int fetchSize) {
        this(name, prev, prev + (long) stepSize * fetchSize, stepSize, fetchSize);
    }

    public BatchIdResult withLast(long newLast) {
        return new BatchIdResult(name, newLast, max, stepSize, fetchSize);
    }

    public BatchIdResult withLastAndMax(long newLast, long newMax) {
        return new BatchIdResult(name, newLast, newMax, stepSize, fetchSize);
    }
}
