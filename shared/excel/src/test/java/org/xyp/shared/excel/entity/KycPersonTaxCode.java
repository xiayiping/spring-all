package org.xyp.shared.excel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycPersonTaxCode {
    Long identityId;
    String country;
    String code;
}
