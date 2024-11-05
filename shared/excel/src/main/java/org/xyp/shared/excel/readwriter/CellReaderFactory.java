package org.xyp.shared.excel.readwriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.xyp.shared.excel.SupportedType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

public class CellReaderFactory {
    private CellReaderFactory() {
    }

    private static final Map<SupportedType, ICellReader> map = new EnumMap<>(SupportedType.class);

    static {
        map.put(SupportedType.BOOLEAN, new BooleanCellReader());
        map.put(SupportedType.DATE, new DateCellReader());
        map.put(SupportedType.DOUBLE, new DoubleCellReader());
        map.put(SupportedType.FLOAT, new FloatCellReader());
        map.put(SupportedType.INT, new IntegerCellReader());
        map.put(SupportedType.LONG, new LongCellReader());
        map.put(SupportedType.STRING, new StringCellReader());
        map.put(SupportedType.LOCAL_DATE, new LocalDateCellReader());
        map.put(SupportedType.LOCAL_DATE_TIME, new LocalDateTimeCellReader());
    }

    public static ICellReader getCellReader(SupportedType type) {
        return map.getOrDefault(type, map.get(SupportedType.STRING));
    }

    private static class StringCellReader implements ICellReader {
        public String readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getStringFromCell(cell, evaluator);
        }
    }

    private static class IntegerCellReader implements ICellReader {
        public Integer readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getIntFromCell(cell, evaluator);
        }
    }

    private static class LongCellReader implements ICellReader {
        public Long readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getLongFromCell(cell, evaluator);
        }
    }

    private static class FloatCellReader implements ICellReader {
        public Float readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getFloatFromCell(cell, evaluator);
        }
    }

    private static class DoubleCellReader implements ICellReader {
        public Double readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getDoubleFromCell(cell, evaluator);
        }
    }

    private static class BooleanCellReader implements ICellReader {
        public Boolean readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getBooleanFromCell(cell, evaluator);
        }
    }

    private static class DateCellReader implements ICellReader {
        public Date readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getDateFromCell(cell, evaluator);
        }
    }

    private static class LocalDateCellReader implements ICellReader {
        public LocalDate readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getLocalDateFromCell(cell, evaluator);
        }
    }

    private static class LocalDateTimeCellReader implements ICellReader {
        public LocalDateTime readFromCell(Cell cell, FormulaEvaluator evaluator) {
            return ExcelCellReader.getLocalDateTimeFromCell(cell, evaluator);
        }
    }

}
