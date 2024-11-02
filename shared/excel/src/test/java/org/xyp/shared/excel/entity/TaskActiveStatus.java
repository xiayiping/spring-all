package org.xyp.shared.excel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TaskActiveStatus {
    UNKNOWN(-1),
    INACTIVE(0),
    ACTIVE(1),
    COVERED(2),
    OVERDUE(3);

    final int code;

    private static final Map<Integer, TaskActiveStatus> map;

    static {
        map = Arrays.stream(TaskActiveStatus.values())
            .collect(Collectors.toMap(TaskActiveStatus::getCode, UnaryOperator.identity()));
    }

    public static TaskActiveStatus fromCode(int code) {
        return Optional.ofNullable(map.get(code)).orElse(UNKNOWN);
    }
}
