package org.xyp.shared.excel.readwriter;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;
import org.xyp.shared.excel.model.ImportColumnModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * except date , return null when the cell is null. boolean returns false if
 * null<br/>
 *
 * @author xiayip
 */
@Slf4j
public class ExcelCellReader {
    private ExcelCellReader() {
    }

    public static Object readValue(Cell cell, ImportColumnModel columnModel, FormulaEvaluator evaluator) {
        return CellReaderFactory.getCellReader(columnModel.getType()).readFromCell(cell, evaluator);
    }

    public static Integer getIntFromCell(Cell cell, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) (cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            val str = cell.getRichStringCellValue().getString();
            if (!StringUtils.hasText(str)) {
                return null;
            }
            return Integer.parseInt(str);
        } else if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            return (int) (cellValue.getNumberValue());
        }
        return null;
    }

    public static Integer getIntFromCell(Row row, int cellIndex, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getIntFromCell(cell, evaluator);
    }

    public static Integer getIntFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                         FormulaEvaluator evaluator) throws NumberFormatException {
        Row row = sheet.getRow(rowIndex);
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getIntFromCell(cell, evaluator);
    }

    public static Long getLongFromCell(Cell cell, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) (cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            val str = cell.getRichStringCellValue().getString();
            if (!StringUtils.hasText(str)) {
                return null;
            }
            return Long.parseLong(str);
        } else if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            return (long) (cellValue.getNumberValue());
        }
        return null;
    }

    public static Long getLongFromCell(Row row, int cellIndex, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getLongFromCell(cell, evaluator);
    }

    public static Long getLongFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                       FormulaEvaluator evaluator) throws NumberFormatException {
        Row row = sheet.getRow(rowIndex);
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getLongFromCell(cell, evaluator);
    }

    public static Float getFloatFromCell(Cell cell, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (float) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            val str = cell.getRichStringCellValue().getString();
            if (!StringUtils.hasText(str)) {
                return null;
            }
            return Float.parseFloat(str);
        } else if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            return (float) cellValue.getNumberValue();
        }
        return null;
    }

    public static Float getFloatFromCell(Row row, int cellIndex, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getFloatFromCell(cell, evaluator);
    }

    public static Float getFloatFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                         FormulaEvaluator evaluator) throws NumberFormatException {
        Row row = sheet.getRow(rowIndex);
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getFloatFromCell(cell, evaluator);
    }

    public static Double getDoubleFromCell(Cell cell, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            val str = cell.getRichStringCellValue().getString();
            if (!StringUtils.hasText(str)) {
                return null;
            }
            return Double.parseDouble(str);
        } else if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            return (cellValue.getNumberValue());
        }
        return null;
    }

    public static Double getDoubleFromCell(Row row, int cellIndex, FormulaEvaluator evaluator)
        throws NumberFormatException {
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getDoubleFromCell(cell, evaluator);
    }

    public static Double getDoubleFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                           FormulaEvaluator evaluator) throws NumberFormatException {
        Row row = sheet.getRow(rowIndex);
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getDoubleFromCell(cell, evaluator);
    }

    public static Boolean getBooleanFromCell(Cell cell, FormulaEvaluator evaluator) {
        return Optional.ofNullable(cell)
            .map(cl -> {
                if (cl.getCellType() == CellType.BOOLEAN) {
                    return (cl.getBooleanCellValue());
                } else if (cl.getCellType() == CellType.NUMERIC) {
                    return (cl.getNumericCellValue() > 0);
                } else if (cl.getCellType() == CellType.FORMULA) {
                    CellValue cellValue = evaluator.evaluate(cl);
                    return cellValue.getBooleanValue();
                } else if (cl.getCellType() == CellType.STRING) {
                    String s = (cl.getRichStringCellValue().getString().trim());
                    if ("TRUE".equalsIgnoreCase(s)) {
                        return Boolean.TRUE;
                    } else if ("FALSE".equalsIgnoreCase(s)) {
                        return Boolean.FALSE;
                    }
                }
                return null;
            })
            .orElse(null);
    }

    public static Boolean getBooleanFromCell(Row row, int cellIndex, FormulaEvaluator evaluator) {
        return Optional.ofNullable(row)
            .map(r -> r.getCell(cellIndex))
            .map(cell -> getBooleanFromCell(cell, evaluator))
            .orElse(null);
    }

    public static Boolean getBooleanFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                             FormulaEvaluator evaluator) {
        return Optional.ofNullable(sheet)
            .map(s -> s.getRow(rowIndex))
            .map(row -> row.getCell(cellIndex))
            .map(cell -> getBooleanFromCell(cell, evaluator))
            .orElse(null);
    }

    public static Date getDateFromCell(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        val cellType = cell.getCellType();
        if (cellType == CellType.STRING && Optional.ofNullable(getStringFromCell(cell, null)).orElse("").isEmpty()) {
            return null;
        }
        if (cellType == CellType.NUMERIC) {
            return (cell.getDateCellValue());
        } else if (cellType == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            long lo = (long) (cellValue.getNumberValue());
            return new Date(lo);
        } else if (cellType == CellType.STRING) {
            val dateString = getStringFromCell(cell, evaluator);
            for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
                try {
                    return Date.from(LocalDateTime.parse(dateString, formatter).atZone(ZoneId.systemDefault()).toInstant());
                } catch (Exception e) {
                    log.warn("trying to parse Date {} using {} failed", dateString, formatter);
                }
            }
        }
        return null;
    }

    public static LocalDate getLocalDateFromCell(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        val cellType = cell.getCellType();
        if (cellType == CellType.STRING && Optional.ofNullable(getStringFromCell(cell, null)).orElse("").isEmpty()) {
            return null;
        }
        if (cellType == CellType.NUMERIC) {
            return LocalDate.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault());
        } else if (cellType == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            long lo = (long) (cellValue.getNumberValue());
            return LocalDate.ofInstant(Instant.ofEpochMilli(lo), ZoneId.systemDefault());
        } else if (cellType == CellType.STRING) {
            val dateString = getStringFromCell(cell, evaluator);
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(dateString, formatter);
                } catch (Exception e) {
                    log.warn("trying to parse LocalDate {} using {} failed", dateString, formatter);
                }
            }
        }
        return null;
    }

    public static LocalDateTime getLocalDateTimeFromCell(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        val cellType = cell.getCellType();
        if (cellType == CellType.STRING && Optional.ofNullable(getStringFromCell(cell, null)).orElse("").isEmpty()) {
            return null;
        }
        if (cellType == CellType.NUMERIC) {
            return LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault());
        } else if (cellType == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            long lo = (long) (cellValue.getNumberValue());
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(lo), ZoneId.systemDefault());
        } else if (cellType == CellType.STRING) {
            val dateString = getStringFromCell(cell, evaluator);
            for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateString, formatter);
                } catch (Exception e) {
                    log.warn("trying to parse LocalDateTime {} using {} failed", dateString, formatter);
                }
            }
        }
        return null;
    }

    private static final List<DateTimeFormatter> DATE_FORMATTERS = new ArrayList<>();

    static {
        // Add all possible formats here
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("M/d/yyyy"));
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // Add more formats as needed
    }

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = new ArrayList<>();

    static {
        // Add all possible formats here
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // Standard ISO format
        // Add more formats as needed
    }

    public static Date getDateFromCell(Row row, int cellIndex, FormulaEvaluator evaluator) {
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getDateFromCell(cell, evaluator);
    }

    public static Date getDateFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                       FormulaEvaluator evaluator) {
        Row row = sheet.getRow(rowIndex);
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getDateFromCell(cell, evaluator);
    }

    public static String getStringFromCell(Cell cell, FormulaEvaluator evaluator) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getRichStringCellValue().getString().trim();
            if (val.isEmpty()) {
                return null;
            }
            return val;
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            return Double.toString(d).replaceAll("\\.0+$", "");
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            Boolean val = getBooleanFromCell(cell, evaluator);
            return val == null ? null : val.toString();
        } else if (cell.getCellType() == CellType.FORMULA && null != evaluator) {
            CellValue cellValue = evaluator.evaluate(cell);
            return (cellValue.getStringValue().trim());
        }

        return null;
    }

    public static String getStringFromCell(Row row, int cellIndex, FormulaEvaluator evaluator) {
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getStringFromCell(cell, evaluator);
    }

    public static String getStringFromCell(Sheet sheet, int rowIndex, int cellIndex,
                                           FormulaEvaluator evaluator) {
        Row row = sheet.getRow(rowIndex);
        if (null == row)
            return null;
        Cell cell = row.getCell(cellIndex);
        if (null == cell)
            return null;
        return getStringFromCell(cell, evaluator);
    }

}
