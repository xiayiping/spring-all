package org.xyp.demo.api.maptransfer;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PropertyUtil {
    private PropertyUtil() {
    }

    public static final String PROPERTY_SPLITTER = ".";

    public static final String ARR_INDEX_PATTERN = "^(.*)\\[(\\d+)\\]$";

    public static Object getProperty(Object object, String property) {

        if (!StringUtils.hasText(property)) {
            return object;
        }

        int nextSplitter = property.indexOf(PROPERTY_SPLITTER);

        var firstProperty = -1 == nextSplitter ? property : property.substring(0, nextSplitter);

        var arrayMatcher = Pattern.compile(ARR_INDEX_PATTERN).matcher(firstProperty);
        boolean isArrayIdx = arrayMatcher.find();

        if (isArrayIdx) {
            var arrProperty = arrayMatcher.group(1);
            var idx = Integer.parseInt(arrayMatcher.group(2));
            var item = getPropertyValueForList(object, arrProperty, idx);
            if (nextSplitter < 0) {
                return item;
            }
            return getProperty(item, property.substring(nextSplitter + 1));
        } else {
            return getPropertyValue(object, property, firstProperty, nextSplitter);
        }

    }

    private static Object getPropertyValue(Object object, String property
            , String firstProperty, int nextSplitter) {
        if (object instanceof Map<?, ?> map) {
            Object nextObject = map.get(firstProperty);
            return (-1 == nextSplitter) ? nextObject
                    : getProperty(nextObject, property.substring(nextSplitter + 1));
        } else {
            Object nextObject = new BeanWrapperImpl(object).getPropertyValue(firstProperty);
            return (-1 == nextSplitter) ? nextObject
                    : getProperty(nextObject, property.substring(nextSplitter + 1));
        }
    }

    private static Object getPropertyValueForList(Object object
            , String firstProperty, int idx) {
        Object items;
        if (object instanceof Map<?, ?> map) {
            items = map.get(firstProperty);
        } else {
            items = new BeanWrapperImpl(object).getPropertyValue(firstProperty);
        }

        if (items instanceof List<?> list) {
            if (list.size() <= idx) {
                throw new IllegalArgumentException("expect size not less than "
                        + idx
                        + " but actual size " + list.size());
            }
            return list.get(idx);
        } else if (items instanceof Iterator<?> iter) {
            return getItemFromIterator(idx, iter);
        } else if (items instanceof Iterable<?> iterable) {
            Iterator<?> iter = iterable.iterator();
            return getItemFromIterator(idx, iter);

        } else {
            throw new IllegalArgumentException("expect iterable, but found " +
                    items + " for property " + firstProperty);
        }
    }

    private static Object getItemFromIterator(int idx, Iterator<?> iter) {
        int i = 0;
        while (iter.hasNext()) {
            var ret = iter.next();
            if (i == idx) {
                return ret;
            }
            i++;
        }
        throw new IllegalArgumentException("expect size not less than "
                + idx
                + " but actual size " + i);
    }
}
