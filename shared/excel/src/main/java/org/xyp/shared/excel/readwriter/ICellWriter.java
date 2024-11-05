package org.xyp.shared.excel.readwriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

public interface ICellWriter<T> {
    void writeToCell(T value, Cell cell, CellStyle style);
}
