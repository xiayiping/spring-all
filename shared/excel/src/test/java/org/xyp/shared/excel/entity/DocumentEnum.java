package org.xyp.shared.excel.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Getter
public enum DocumentEnum {
    UNKNOWN(-1),
    IDENTITY_PROOF(1),
    ADDRESS_PROOF(2),
    VERIFICATION_FILE(3);

    private final int code;

    private static final Map<Integer, DocumentEnum> map;

    static {
        map = Arrays.stream(DocumentEnum.values())
            .collect(Collectors.toMap(DocumentEnum::getCode, UnaryOperator.identity()));
    }

    public static DocumentEnum fromCode(int code) {
        return Optional.ofNullable(map.get(code)).orElse(UNKNOWN);
    }

    DocumentEnum(int code) {
        this.code = code;
    }

}
