package org.xyp.sample.spring.webapi.domain.task.pojo;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.BatchRule;

import java.util.List;
import java.util.Optional;

@Builder
public record BatchPojo(
    @JsonIgnore
    Batch.BatchId id,
    int companyId,
    String batchName,
    List<BatchRule> batchRules
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long getBatchId() {
        return Optional.ofNullable(id)
            .map(Batch.BatchId::id)
            .orElse(null);
    }

    public static BatchPojo from(Batch batch) {
        return BatchPojo.builder()
            .id(batch.getId())
            .companyId(batch.getCompanyId())
            .batchName(batch.getBatchName())
            .batchRules(batch.getBatchRules().stream().toList())
            .build()
            ;
    }

    @JsonCreator
    public BatchPojo(
        @JsonProperty("batchId") long id,
        @JsonProperty("companyId") int companyId,
        @JsonProperty("batchName") String batchName,
        @JsonProperty("batchRules") List<BatchRule> batchRules
    ) {
        this(
            Batch.BatchId.of(id),
            companyId,
            batchName,
            batchRules
        );
    }
}
