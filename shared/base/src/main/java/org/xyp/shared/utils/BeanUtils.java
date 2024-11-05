package org.xyp.shared.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
public class BeanUtils {
    private BeanUtils() {
    }

    public static Object propertyValue(Object target, String key)
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String[] keys = key.split("\\.");
        return propertyValue(target, keys, new ArrayList<>());
    }

    private static Object propertyValue(Object target, String[] keys, ArrayList<String> processed)
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (keys.length == 1) {
            return singlePropertyValue(target, keys[0]);
        } else {
            String[] newKeys = new String[keys.length - 1];
            System.arraycopy(keys, 1, newKeys, 0, newKeys.length);
            processed.add(keys[0]);
            val subTarget = singlePropertyValue(target, keys[0]);
            if (null == subTarget) {
                final var fields = String.join(".", processed);
                log.warn("intermediate property value is null for {}", fields);
                return null;
            }
            return propertyValue(subTarget, newKeys, processed);
        }
    }

    private static Object singlePropertyValue(Object target, String key)
        throws InvocationTargetException, IllegalAccessException {
        final Optional<Field> fieldOpt = getField(target, key);
        if (fieldOpt.isPresent()) {
            val field = fieldOpt.get();
            field.setAccessible(true);
            return field.get(target);
        } else {
            final var methodOpt = Optional.ofNullable(
                getMethod(target, "get" + upperCaseFirstLetter(key))
                    .orElseGet(() -> getMethod(target, "is" + upperCaseFirstLetter(key))
                        .orElseGet(() -> getMethod(target, key)
                            .orElse(null))
                    ));

            if (methodOpt.isPresent()) {
                final var method = methodOpt.get();
                method.setAccessible(true);
                return method.invoke(target);
            } else {
                log.warn("{} doesn't exist in target type {}", key, target.getClass().getName());
                return null;
            }
        }
    }

    private static String upperCaseFirstLetter(String key) {
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    private static Optional<Field> getField(Object target, String fieldName) {
        Class<?> clazz = target.getClass();
        try {
            return Optional.of(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    private static Optional<Method> getMethod(Object target, String methodName) {
        Class<?> clazz = target.getClass();
        try {
            return Optional.of(clazz.getMethod(methodName));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
