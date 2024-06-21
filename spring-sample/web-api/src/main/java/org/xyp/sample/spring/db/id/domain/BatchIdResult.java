package org.xyp.sample.spring.db.id.domain;

public record BatchIdResult(
    long last,
    long max,
    int step,
    int fetchSize
) {
}
