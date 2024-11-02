package org.xyp.shared.excel;

import lombok.val;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.InvocationTargetException;

public class BeanUtil {

    private BeanUtil() {
    }

    public static Object propertyValue(Object target, String key)
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String[] keys = key.split("\\.");
        return propertyValue(target, keys);
    }

    private static Object propertyValue(Object target, String[] keys)
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (keys.length == 1) {
            return singlePropertyValue(target, keys[0]);
        } else {
            String[] newKeys = new String[keys.length - 1];
            System.arraycopy(keys, 1, newKeys, 0, newKeys.length);
            val subTarget = singlePropertyValue(target, keys[0]);
            return propertyValue(subTarget, newKeys);
        }
    }

    private static Object singlePropertyValue(Object target, String key)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BeanWrapper beanWrapper = new BeanWrapperImpl(target);
        if (beanWrapper.isReadableProperty(key)) {
            return beanWrapper.getPropertyValue(key);
        } else {
            return target.getClass()
                .getMethod("get" + upperCaseFirstLetter(key))
                .invoke(target);
        }
    }

    private static String upperCaseFirstLetter(String key) {
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }
}
