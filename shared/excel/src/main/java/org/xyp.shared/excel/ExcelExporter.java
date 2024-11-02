package org.xyp.shared.excel;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.xyp.shared.excel.model.ExcelCellStyle;
import org.xyp.shared.excel.model.ExportColumnModel;
import org.xyp.shared.excel.model.ExportSheetModel;
import org.xyp.shared.excel.readwriter.ExcelCellWriter;
import org.xyp.shared.function.wrapper.ResultOrError;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ExcelExporter implements IExport<Workbook> {

    private final ExportConfigurationContainer configContainer;

    public ExcelExporter(ExportConfigurationContainer container) {
        this.configContainer = container;
    }

    public static ExcelExporter newInstance(ExportConfigurationContainer container) {
        return new ExcelExporter(container);
    }

    @Override
    public XSSFWorkbook exportMapList(String configPath, List<? extends Map<String, Object>> values) {
        ExportSheetModel sheetModel = getSheetModel(configPath);
        return exportMapList(sheetModel, values);
    }

    @Override
    public <E> XSSFWorkbook exportObjectList(String configPath, List<? extends E> values) {
        ExportSheetModel sheetModel = getSheetModel(configPath);
        return exportObjectList(sheetModel, values);
    }

    public XSSFWorkbook exportObjectList(Map<String, List<Object>> configPathsValues) {
        val workbook = new XSSFWorkbook();
        Map<ExcelCellStyle, CellStyle> titleStyleMap = new HashMap<>();
        DataFormat format = workbook.createDataFormat();

        configPathsValues.forEach((configPath, list) -> {
            ExportSheetModel sheetModel = getSheetModel(configPath);
            val sheet = workbook.createSheet(sheetModel.getName());
            exportObjectList(sheetModel, list, workbook, sheet, titleStyleMap, format);
        });

        return workbook;
    }

    @Override
    public XSSFWorkbook exportMapList(ExportSheetModel sheetModel,
                                      List<? extends Map<String, Object>> values) {
        val workbook = new XSSFWorkbook();
        DataFormat format = workbook.createDataFormat();
        val sheet = workbook.createSheet(sheetModel.getName());

        Map<ExcelCellStyle, CellStyle> titleStyleMap = new HashMap<>();

        writeMapToSheet(sheetModel, workbook, sheet, format, values,
            sheetModel.getStartRow(),
            titleStyleMap);
        return workbook;
    }

    /**********************************/
    @Override
    public <E> XSSFWorkbook exportObjectList(ExportSheetModel sheetModel, List<? extends E> values) {
        val workbook = new XSSFWorkbook();
        Map<ExcelCellStyle, CellStyle> titleStyleMap = new HashMap<>();
        DataFormat format = workbook.createDataFormat();
        val sheet = workbook.createSheet(sheetModel.getName());
        writeObjectToSheet(sheetModel, workbook, sheet, format, values, titleStyleMap);
        return workbook;
    }

    public <E> Workbook exportObjectList(
        ExportSheetModel sheetModel,
        List<? extends E> values,
        Workbook workbook,
        Sheet sheet,
        Map<ExcelCellStyle, CellStyle> titleStyleMap,
        DataFormat format
    ) {
        writeObjectToSheet(sheetModel, workbook, sheet, format, values, titleStyleMap);
        return workbook;
    }

    @Override
    public XSSFWorkbook exportMapLists(String[] configPathes,
                                       List<? extends List<? extends Map<String, Object>>> values) {
        List<ExportSheetModel> exportSheetModels = getSheetModels(configPathes);
        Iterator<ExportSheetModel> sheetModelIter = exportSheetModels.iterator();
        Iterator<? extends List<? extends Map<String, Object>>> valuesIter = values.iterator();
        XSSFWorkbook workbook = new XSSFWorkbook();
        DataFormat format = workbook.createDataFormat();
        Sheet sheet = null;

        Map<ExcelCellStyle, CellStyle> titleStyleMap = new HashMap<>();
        Map<String, Integer> sheetNameCount = new HashMap<>();
        while (sheetModelIter.hasNext()) {
            ExportSheetModel sheetModel = sheetModelIter.next();
            final int startRow = sheetModel.getStartRow();
            List<? extends Map<String, Object>> value = valuesIter.next();
            sheet = createSheet(workbook, sheetNameCount, sheetModel);
            writeMapToSheet(sheetModel, workbook, sheet, format, value, startRow, titleStyleMap);
        }
        return workbook;
    }

    @Override
    public <E> XSSFWorkbook exportObjectLists(String[] configPathes,
                                              List<? extends List<? extends E>> values
    ) {
        List<ExportSheetModel> exportSheetModels = getSheetModels(configPathes);
        Iterator<ExportSheetModel> sheetModelIter = exportSheetModels.iterator();
        Iterator<? extends List<? extends E>> valuesIter = values.iterator();
        XSSFWorkbook workbook = new XSSFWorkbook();
        DataFormat format = workbook.createDataFormat();
        Map<ExcelCellStyle, CellStyle> titleStyleMap = new HashMap<>();
        Map<String, Integer> sheetNameCount = new HashMap<>();
        while (sheetModelIter.hasNext()) {
            ExportSheetModel sheetModel = sheetModelIter.next();
            List<? extends E> value = valuesIter.next();
            Sheet sheet = createSheet(workbook, sheetNameCount, sheetModel);
            writeObjectToSheet(sheetModel, workbook, sheet, format, value, titleStyleMap);
        }
        return workbook;
    }

    private List<CellStyle> getStyles(Workbook workbook, ExportSheetModel sheetModel,
                                      DataFormat format
    ) {
        List<CellStyle> styles = new ArrayList<>();
        for (ExportColumnModel cm : sheetModel.getColumnList()) {
            Optional.ofNullable(cm.getExcelFormat())
                .ifPresentOrElse(value -> {
                        val style = workbook.createCellStyle();
                        style.setDataFormat(format.getFormat(value));
                        styles.add(style);
                    }
                    , () -> styles.add(null));
        }

        return styles;
    }

    private int writeMapToSheet(ExportSheetModel sheetModel, Workbook workbook,
                                Sheet sheet, DataFormat format, List<? extends Map<String, Object>> values,
                                int startRow,
                                Map<ExcelCellStyle, CellStyle> titleStyleMap) {
        startRow = writeTitle(sheetModel, workbook, sheet, startRow, titleStyleMap);
        List<CellStyle> styles = getStyles(workbook, sheetModel, format);
        startRow = writeMapData(sheetModel, sheet, styles, values, startRow);
        return startRow;
    }

    private <E> int writeObjectToSheet(ExportSheetModel sheetModel, Workbook workbook,
                                       Sheet sheet, DataFormat format, List<? extends E> values,
                                       Map<ExcelCellStyle, CellStyle> titleStyleMap
    ) {
        var startRow = sheetModel.getStartRow();
        startRow = writeTitle(sheetModel, workbook, sheet, startRow, titleStyleMap);
        List<CellStyle> styles = getStyles(workbook, sheetModel, format);
        startRow = writeObjectData(sheetModel, sheet, styles, values, startRow);
        return startRow;
    }

    private int writeMapData(ExportSheetModel sheetModel, Sheet sheet,
                             List<CellStyle> styles, List<? extends Map<String, Object>> values,
                             int startRow
    ) {
        Iterator<? extends Map<String, Object>> valueIter = values.iterator();

        Map<Integer, Object> currentValueMap = new HashMap<>();

        while (valueIter.hasNext()) {

            Map<String, Object> valueMap = valueIter.next();

            Row row = sheet.createRow(startRow++);
            Iterator<ExportColumnModel> columnModelIter = sheetModel.getColumnList().iterator();
            Iterator<CellStyle> cellStyleIter = styles.iterator();
            int colIndex = 0;
            while (columnModelIter.hasNext()) {
                ExportColumnModel columnModel = columnModelIter.next();
                if (columnModel.isEmpty()) {
                    colIndex++;
                    continue;
                }
                colIndex = Optional.ofNullable(columnModel.getColIdx()).orElse(colIndex);
                CellStyle cellStyle = cellStyleIter.next();
                Object value = valueMap.get(columnModel.getKey());
                try {
                    writeValue(currentValueMap, row, colIndex, columnModel, cellStyle, value);
                } finally {
                    colIndex++;
                }

            }
        }
        return startRow;
    }

    private void writeValue(
        Map<Integer, Object> currentValueMap
        , Row row, int colIndex
        , ExportColumnModel columnModel
        , CellStyle cellStyle, Object value
    ) {
        if (null != value) {
            // check for duplicate value
            Object lastValue = currentValueMap.get(colIndex);

            if (columnModel.isAllowDuplicate() || !value.equals(lastValue)) {
                Cell c = row.createCell(colIndex);
                ExcelCellWriter.writeToCell(value, c, cellStyle);
                currentValueMap.put(colIndex, value);
            }
        }
    }

    private <E> int writeObjectData(ExportSheetModel sheetModel, Sheet sheet,
                                    List<CellStyle> styles, List<? extends E> values,
                                    int startRowInput
    ) {
        Map<Integer, Object> currentValueMap = new HashMap<>();
        AtomicInteger startRow = new AtomicInteger(startRowInput);

        values.forEach(valueMap -> {
            Row row = sheet.createRow(startRow.getAndIncrement());
            Iterator<ExportColumnModel> columnModelIter = sheetModel.getColumnList().iterator();
            Iterator<CellStyle> cellStyleIter = styles.iterator();
            int colIndex = 0;

            while (columnModelIter.hasNext()) {
                ExportColumnModel columnModel = columnModelIter.next();
                if (columnModel.isEmpty()) {
                    colIndex++;
                    cellStyleIter.next();
                    continue;
                }
                colIndex = Optional.ofNullable(columnModel.getColIdx()).orElse(colIndex);
                CellStyle cellStyle = cellStyleIter.next();

                Object value = ResultOrError.on(() -> {
                        String key = columnModel.getKey();
                        return BeanUtil.propertyValue(valueMap, key);
                    }).getResult()
                    .getOrFallBackForError(ex -> {
                        log.trace("{}  Use null", ex.getMessage());
                        return null;
                    });

                writeValue(currentValueMap, row, colIndex, columnModel, cellStyle, value);
                colIndex++;
            }
        });
        return startRow.get();
    }

    /**
     * @param sheetModel sheetModel
     * @param sheet      sheet
     * @param startRow   startRow
     * @return the next row index to write data or title
     */
    private int writeTitle(ExportSheetModel sheetModel, Workbook book, Sheet sheet,
                           int startRow,
                           Map<ExcelCellStyle, CellStyle> titleStyleMap
    ) {

        Row titleRow = sheet.createRow(startRow++);
        Iterator<ExportColumnModel> colModelIter = sheetModel.getColumnList().iterator();
        int colIndex = 0;
        while (colModelIter.hasNext()) {
            ExportColumnModel cm = colModelIter.next();
            colIndex = Optional.ofNullable(cm.getColIdx()).orElse(colIndex);
            val colWidth = Optional.ofNullable(cm.getColWidth()).orElse(ExportColumnModel.DEFAULT_COL_WIDTH);
            sheet.setColumnWidth(colIndex, colWidth * 256);

            Cell c = titleRow.createCell(colIndex++);
            ExcelCellWriter.writeToCell(cm.getTitle(), c, null);
            val titleColor = Optional.ofNullable(cm.getTitleColor()).orElseGet(sheetModel::getTitleColor);
            val titleFont = Optional.ofNullable(cm.getTitleFont()).orElseGet(sheetModel::getTitleFont);
            val titleBold = Optional.ofNullable(cm.getTitleBold()).orElseGet(sheetModel::getTitleBold);
            val titleFontSize = Optional.ofNullable(cm.getTitleFontSize()).orElseGet(sheetModel::getTitleFontSize);
            val excelCellStyle = new ExcelCellStyle(
                titleColor, titleFont, titleBold, titleFontSize
            );

            if (!excelCellStyle.isEmpty()) {
                val titleStyle = titleStyleMap.computeIfAbsent(excelCellStyle, styleKey -> {
                    val style = sheet.getWorkbook().createCellStyle();

                    Optional.ofNullable(excelCellStyle.bgColor()).ifPresent(tColor -> {
                        style.setFillForegroundColor(IndexedColors.valueOf(tColor).getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    });
                    if (excelCellStyle.needFont()) {
                        Font font = book.createFont();
                        Optional.ofNullable(excelCellStyle.font()).ifPresent(font::setFontName);
                        Optional.ofNullable(excelCellStyle.fontSize()).ifPresent(font::setFontHeightInPoints);
                        Optional.ofNullable(excelCellStyle.fontBold())
                            .filter(b -> b)
                            .ifPresent(isBold -> font.setBold(true));
                        style.setFont(font);
                    }

                    return style;
                });
                c.setCellStyle(titleStyle);
            }
        }
        return startRow;
    }

    private Sheet createSheet(
        XSSFWorkbook workbook
        , Map<String, Integer> sheetNameCount
        , ExportSheetModel sheetModel
    ) {
        Sheet sheet;
        String sn = sheetModel.getName();
        Integer ct = sheetNameCount.get(sn);
        if (null != ct) {
            ct = ct + 1;
            sheetNameCount.put(sn, ct);
            sn = sn + "_" + ct;
        } else {
            ct = 1;
            sheetNameCount.put(sn, ct);
        }
        sheet = workbook.createSheet(sn);
        return sheet;
    }

    private ExportSheetModel getSheetModel(String configPath) {
        return this.configContainer.getExportSheetModel(configPath);
    }

    private List<ExportSheetModel> getSheetModels(String[] configPathes) {
        LinkedList<ExportSheetModel> models = new LinkedList<>();
        for (String configPath1 : configPathes) {
            ExportSheetModel config1 = getSheetModel(configPath1);
            models.addLast(config1);
        }
        return models;
    }

}
