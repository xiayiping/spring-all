package org.xyp.shared.excel.readwriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

public class ExcelCellWriter {
    private ExcelCellWriter() {
    }

    public static void writeToCell(Object value, Cell cell, CellStyle style) {
        if (value == null || cell == null)
            return;

        final ICellWriter<Object> writer = CellWriterFactory.getCellWriter(value.getClass());
        writer.writeToCell(value, cell, style);

        if (style != null) {
            cell.setCellStyle(style);
        }
    }

}
