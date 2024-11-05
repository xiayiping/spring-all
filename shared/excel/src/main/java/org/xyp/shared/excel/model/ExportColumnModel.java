package org.xyp.shared.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ExportColumnModel {
    public static final Integer DEFAULT_COL_WIDTH = 10;

    final String key;
    final String title;
    final String titleColor;
    final Boolean titleBold;
    final String titleFont;
    final Short titleFontSize;
    final String excelFormat;
    final Integer colIdx;
    final Integer colWidth;
    final boolean empty;
    private final boolean allowDuplicate;

    public ExportColumnModel(ExportColumnModel other) {
        this.key = other.key;
        this.title = other.title;
        this.titleColor = other.titleColor;
        this.titleBold = other.titleBold;
        this.titleFont = other.titleFont;
        this.titleFontSize = other.titleFontSize;
        this.excelFormat = other.excelFormat;
        this.colIdx = other.colIdx;
        this.colWidth = other.colWidth;
        this.empty = other.empty;
        this.allowDuplicate = other.allowDuplicate;
    }

    public String getTitle() {
        if (null == title) {
            return key;
        }
        return title;
    }

}
