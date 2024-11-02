package org.xyp.shared.excel.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Getter
public enum IdProofDocEnum {
    UNKNOWN(-1, "Unknown"),
    ID_CARD(1, "National identification card"),
    PASSPORT(2,"Passport"),
    MC_ID(3, "Permanent Resident Identity Card of Macau Special Administrative Region"), // 澳門特別行政區永久居民身分證
    TW_ID(4, "Mainland Travel Permit for Taiwan Residents"), // 台灣居民來往大陸通行證
    SEA_MAN_ID(5, "Seaman’s Identity Document"), // 海員身分證明文件
    ML_TW_PERM(6, "Taiwan Travel Permit for Mainland Residents"), // 大陸居民往來台灣通行證
    MC_RES_ISS_DI(7, "Permit for residents of Macau issued by Director of Immigration"), // 由入境事務處處長簽發的澳門居民旅遊證
    HK_MC_TRAVEL_OFFICIAL(8, "Exit-entry Permit for Travelling to and from Hong Kong and Macau for Official Purposes"), // 因公往來香港澳門特別行政區通行證
    HK_MC_TRAVEL(9, "Exit-entry Permit for Travelling to and from Hong Kong and Macau"), // 往來港澳通行證
    OTHERS(999, "Others");

    private final int code;
    private final String englishName;

    private static final Map<Integer, IdProofDocEnum> map;

    static {
        map = Arrays.stream(IdProofDocEnum.values())
            .collect(Collectors.toMap(IdProofDocEnum::getCode, UnaryOperator.identity()));
    }

    public static IdProofDocEnum fromCode(int code) {
        return Optional.ofNullable(map.get(code)).orElse(UNKNOWN);
    }

    IdProofDocEnum(int code, String englishName) {
        this.code = code;
        this.englishName = englishName;
    }
}
