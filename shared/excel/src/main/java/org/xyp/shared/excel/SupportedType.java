package org.xyp.shared.excel;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Getter
public enum SupportedType {
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    BOOLEAN(Boolean.class),
    STRING(String.class),
    DATE(java.util.Date.class),
    LOCAL_DATE(java.time.LocalDate.class),
    LOCAL_DATE_TIME(java.time.LocalDateTime.class);

    private final Class<?> supportedClass;

    SupportedType(Class<?> supportedClass) {
        this.supportedClass = supportedClass;
    }

    private static final Map<Class<?>, SupportedType> map;

    static {
        map = Arrays.stream(SupportedType.values())
            .collect(Collectors.toMap(SupportedType::getSupportedClass, UnaryOperator.identity()));
    }

    public static boolean isSupported(Class<?> clz) {
        return map.containsKey(clz);
    }
}
