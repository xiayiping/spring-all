package org.xyp.shared.excel.model;

public record ExcelCellStyle(
    String bgColor,
    String font,
    Boolean fontBold,
    Short fontSize
) {
    public boolean isEmpty() {
        return null == bgColor
            && null == font
            && null == fontBold
            && null == fontSize;
    }

    public boolean needFont() {
        return !(null == font
            && null == fontBold
            && null == fontSize);
    }

}

