package org.xyp.shared.excel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycTask {
    Long id;
    Integer companyId;
    String employeeId;
    KycBatch kycBatch;
    String companyName;

    KycPersonIdentity idInfo;
    KycPersonAddress addressInfo;
    List<FileEntity> files;
    KycFlowStatusEnum status;
    @Builder.Default
    TaskActiveStatus activeStatus = TaskActiveStatus.ACTIVE;

    LocalDateTime lastUpdateTime;
    String lastUpdatedBy;

}
