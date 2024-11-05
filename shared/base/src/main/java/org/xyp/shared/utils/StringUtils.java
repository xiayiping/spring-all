package org.xyp.shared.utils;

public class StringUtils {
    private StringUtils() {}


    public static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
