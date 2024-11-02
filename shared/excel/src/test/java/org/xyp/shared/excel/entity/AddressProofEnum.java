package org.xyp.shared.excel.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Getter
public enum AddressProofEnum {
    UNKNOWN(-1),
    BANK_STATEMENT(1), // 银行账单
    UTILITY_BILL(2), // 水电煤账单
    TEL_BILL(3), // 電話帳單
    GOV_DOC(4), // 政府通知單
    CH_DRIVE_LISC(5), // 中國駕照
    CH_RES_PERM(6), // 中國居住證
    OTHERS(999); // others

    private final int code;

    private static final Map<Integer, AddressProofEnum> map;

    static {
        map = Arrays.stream(AddressProofEnum.values())
            .collect(Collectors.toMap(AddressProofEnum::getCode, UnaryOperator.identity()));
    }

    public static AddressProofEnum fromCode(int code) {
        return Optional.ofNullable(map.get(code)).orElse(UNKNOWN);
    }

    AddressProofEnum(int code) {
        this.code = code;
    }

}
