package org.xyp.shared.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.xyp.shared.excel.SupportedType;

@Data
@AllArgsConstructor
@Builder
public class ImportColumnModel {
    final String key;
    final SupportedType type;
    final Integer colIdx;

    public ImportColumnModel(ImportColumnModel from) {
        this.key = from.key;
        this.type = from.type;
        this.colIdx = from.colIdx;
    }

    public ImportColumnModel(String key, String type, Integer colIdx) {
        this.key = key.trim();
        this.type = SupportedType.valueOf(type);
        this.colIdx = colIdx;
    }

}
