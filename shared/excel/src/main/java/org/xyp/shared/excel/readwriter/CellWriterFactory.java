package org.xyp.shared.excel.readwriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.xyp.shared.function.wrapper.ResultOrError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CellWriterFactory {
    private CellWriterFactory() {
    }

    private static final Map<Class<?>, ICellWriter<?>> map = new HashMap<>();

    static {
        map.put(String.class, new StringWriter());
        map.put(Date.class, new DateWriter());
        map.put(LocalDate.class, new LocalDateWriter());
        map.put(LocalDateTime.class, new LocalDateTimeWriter());
        map.put(Integer.class, new NumberWriter());
        map.put(Long.class, new NumberWriter());
        map.put(Short.class, new NumberWriter());
        map.put(Float.class, new NumberWriter());
        map.put(Double.class, new NumberWriter());
        map.put(BigDecimal.class, new BigDecimalWriter());
        map.put(Object.class, new ObjectWriter());
    }

    @SuppressWarnings("unchecked")
    public static ICellWriter<Object> getCellWriter(Class<?> clz) {
        return ResultOrError.on(() -> (ICellWriter<Object>) map.getOrDefault(clz, map.get(Object.class)))
            .get();
    }

    private static class StringWriter implements ICellWriter<String> {
        @Override
        public void writeToCell(String value, Cell cell, CellStyle style) {
            cell.setCellValue(value);
        }
    }

    private static class DateWriter implements ICellWriter<Date> {
        @Override
        public void writeToCell(Date value, Cell cell, CellStyle style) {
            cell.setCellValue(value);
        }
    }

    private static class LocalDateWriter implements ICellWriter<LocalDate> {
        @Override
        public void writeToCell(LocalDate value, Cell cell, CellStyle style) {
            cell.setCellValue(value);
        }
    }

    private static class LocalDateTimeWriter implements ICellWriter<LocalDateTime> {
        @Override
        public void writeToCell(LocalDateTime value, Cell cell, CellStyle style) {
            cell.setCellValue(value);
        }
    }

    private static class NumberWriter implements ICellWriter<Number> {
        @Override
        public void writeToCell(Number value, Cell cell, CellStyle style) {
            if (value instanceof Integer || value instanceof Long || value instanceof Short) {
                cell.setCellValue(value.longValue());
            } else {
                cell.setCellValue(value.doubleValue());
            }
        }
    }

    private static class BigDecimalWriter implements ICellWriter<BigDecimal> {
        @Override
        public void writeToCell(BigDecimal value, Cell cell, CellStyle style) {
            cell.setCellValue(value.doubleValue());
        }
    }

    private static class ObjectWriter implements ICellWriter<Object> {
        @Override
        public void writeToCell(Object value, Cell cell, CellStyle style) {
            cell.setCellValue(value.toString());
        }
    }
}
