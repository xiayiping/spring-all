package org.xyp.shared.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@Builder
public class ExportSheetModel {
    final String name;
    final String titleColor;
    final Boolean titleBold;
    final String titleFont;
    final Short titleFontSize;
    final int startRow;
    List<ExportColumnModel> columnList;

    public ExportSheetModel(ExportSheetModel other) {
        List<ExportColumnModel> columns = Optional.ofNullable(this.columnList)
            .orElseGet(List::of).stream().map(ExportColumnModel::new).toList();
        this.name = other.name;
        this.titleColor = other.titleColor;
        this.titleBold = other.titleBold;
        this.titleFont = other.titleFont;
        this.titleFontSize = other.titleFontSize;
        this.startRow = other.startRow;
        this.columnList = columns;
    }


}
