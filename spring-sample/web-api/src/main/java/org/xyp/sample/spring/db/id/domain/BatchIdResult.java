package org.xyp.sample.spring.db.id.domain;

public record BatchIdResult(long prev, long last, int step, int fetchSize) {
}
