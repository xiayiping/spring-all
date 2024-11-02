package org.xyp.shared.excel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycBatch {
    Long id;
    Integer companyId;
    String batchName;
    LocalDate startDate;
    LocalDate endDate;
    Integer validPeriodInDay;
    LocalDateTime creationDateTime;
    String createdBy;
    String employeeGroup;

}
