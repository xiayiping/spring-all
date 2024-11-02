package org.xyp.shared.excel.readwriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public interface ICellReader {
    Object readFromCell(Cell cell, FormulaEvaluator evaluator);
}
