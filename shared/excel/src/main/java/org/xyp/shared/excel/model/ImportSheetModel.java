package org.xyp.shared.excel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * if no sheet name provided, sheet index will be used. Otherwise only uses
 * sheet name.
 *
 * @author xiayip
 */
@Data
@AllArgsConstructor
public class ImportSheetModel {
    final String sheetName;
    final int sheetIndex;
    final int startRow;
    final int endRow;
    final List<ImportColumnModel> columnList;

    public ImportSheetModel(ImportSheetModel other) {
        List<ImportColumnModel> list =
            other.columnList.stream().map(ImportColumnModel::new).toList();
        this.sheetName = other.sheetName;
        this.sheetIndex = other.sheetIndex;
        this.startRow = other.startRow;
        this.endRow = other.endRow;
        this.columnList = list;
    }
}
