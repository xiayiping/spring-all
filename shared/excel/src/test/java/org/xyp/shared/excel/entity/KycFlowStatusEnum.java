package org.xyp.shared.excel.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Getter
public enum KycFlowStatusEnum {
    UNKNOWN(-1),
    PARTICIPANT_INPUT(0),
    COMPANY_CHECKING(30),
    COMPANY_CHECK_APPROVED(40),
    COMPANY_CHECK_REJECTED(50),
    TCT_CHECKING(60),
    TCT_CHECK_APPROVED(70),
    TCT_CHECK_REJECTED(80),
    COMPANY_VERIFY_FILES(90),
    TCT_CHECK_VERIFY_FILES(100),
    FINISHED(9999);

    private final int code;

    private static final Map<Integer, KycFlowStatusEnum> map;

    static {
        map = Arrays.stream(KycFlowStatusEnum.values())
            .collect(Collectors.toMap(KycFlowStatusEnum::getCode, UnaryOperator.identity()));
    }

    public static KycFlowStatusEnum fromCode(int code) {
        return Optional.ofNullable(map.get(code)).orElse(UNKNOWN);
    }

    KycFlowStatusEnum(int code) {
        this.code = code;
    }

}
