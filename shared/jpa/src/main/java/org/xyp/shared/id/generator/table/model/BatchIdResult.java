package org.xyp.shared.id.generator.table.model;


public record BatchIdResult(
    String name,
    long prev,
    long max,
    int stepSize,
    long fetchSize
) {

    public static BatchIdResult fromPrev(String name, long prev, int stepSize, int fetchSize) {
        return new BatchIdResult(name, prev, prev + (long) stepSize * fetchSize, stepSize, fetchSize);
    }

    public BatchIdResult withLast(long newLast) {
        return new BatchIdResult(name, newLast, max, stepSize, fetchSize);
    }

    public BatchIdResult withLastAndMax(long newLast, long newMax) {
        return new BatchIdResult(name, newLast, newMax, stepSize, fetchSize);
    }
}
