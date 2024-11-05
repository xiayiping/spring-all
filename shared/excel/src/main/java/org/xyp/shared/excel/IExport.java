package org.xyp.shared.excel;



import org.xyp.shared.excel.model.ExportSheetModel;

import java.util.List;
import java.util.Map;

public interface IExport<T> {
    T exportMapList(String configPath, List<? extends Map<String, Object>> values);

    <E> T exportObjectList(String configPath, List<? extends E> values);

    T exportMapList(ExportSheetModel sheetModel, List<? extends Map<String, Object>> values);

    <E> T exportObjectList(ExportSheetModel sheetModel, List<? extends E> values);

    T exportMapLists(String[] configPathes,
                     List<? extends List<? extends Map<String, Object>>> values);

    <E> T exportObjectLists(String[] configPathes, List<? extends List<? extends E>> values);

}
