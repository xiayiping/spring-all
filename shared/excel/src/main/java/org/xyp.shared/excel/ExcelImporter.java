package org.xyp.shared.excel;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.xyp.shared.excel.model.ImportColumnModel;
import org.xyp.shared.excel.model.ImportSheetModel;
import org.xyp.shared.excel.readwriter.ExcelCellReader;
import org.xyp.shared.function.wrapper.ResultOrError;
import org.xyp.shared.function.wrapper.WithCloseable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Slf4j
@Component
public class ExcelImporter implements IImport {

    private final ImportConfigurationContainer configContainer;

    public ExcelImporter(ImportConfigurationContainer container) {
        this.configContainer = container;
    }

    public static ExcelImporter newInstance(ImportConfigurationContainer container) {
        return new ExcelImporter(container);
    }

    @Override
    public List<Map<String, Object>> importFromSourceToMap(byte[] bytes, String configName) {
        return ResultOrError.on(() -> {
            try (val is = new ByteArrayInputStream(bytes);
                 XSSFWorkbook workbook = new XSSFWorkbook(is);
            ) {
                ImportSheetModel sheetModel = getImportSheetModel(configName);
                return getMapFromWorkBook(workbook, sheetModel, null);
            }
        }).get();
    }

    public WithCloseable<Workbook, List<Map<String, Object>>>
    importFromSourceToMapKeepOpenWorkbook(Workbook workbook, String configName, String rowField) {
        return WithCloseable.open(() -> workbook)
            .map(book -> {
                ImportSheetModel sheetModel = getImportSheetModel(configName);
                return getMapFromWorkBook(book, sheetModel, rowField);
            });
    }

    @Override
    public List<Map<String, Object>> importFromSourceToMap(File source, String configName) {
        return ResultOrError.on(() -> {
            try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(source));) {
                ImportSheetModel sheetModel = getImportSheetModel(configName);
                return getMapFromWorkBook(workbook, sheetModel, null);
            }
        }).get();
    }

    private List<Map<String, Object>> getMapFromWorkBook(
        Workbook workbook,
        ImportSheetModel sheetModel,
        String excelRowField
    ) {

        String sheetName = null;
        Sheet sheet = null;
        sheetName = sheetModel.getSheetName();
        int sheetIndex = sheetModel.getSheetIndex();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        if (sheetName != null) {
            sheet = workbook.getSheet(sheetName);
        } else {
            sheet = workbook.getSheetAt(sheetIndex);
        }
        LinkedList<Map<String, Object>> list = new LinkedList<>();
        int startRow = sheetModel.getStartRow();
        int lastRow = Optional.of(sheetModel.getEndRow()).filter(i -> i >= 0).orElse(sheet.getLastRowNum());
        for (int i = startRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (null == row)
                break;
            Map<String, Object> map = new HashMap<>();
            if (null != excelRowField) {
                map.put(excelRowField, row);
            }
            list.addLast(map);
            List<ImportColumnModel> columnList = sheetModel.getColumnList();
            Iterator<ImportColumnModel> it = columnList.iterator();
            int j = 0;
            while (it.hasNext()) {
                ImportColumnModel columnModel = it.next();
                j = Optional.ofNullable(columnModel.getColIdx()).orElse(j);
                Cell cell = row.getCell(j);
                val value = ExcelCellReader.readValue(cell, columnModel, evaluator);
                map.put(columnModel.getKey(), value);
                j++;
            }
        }
        return list;

    }

    private ImportSheetModel getImportSheetModel(String configName) {
        return this.configContainer.getImportSheetModel(configName);
    }

}
