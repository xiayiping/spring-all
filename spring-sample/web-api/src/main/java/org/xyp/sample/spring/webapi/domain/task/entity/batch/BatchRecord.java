package org.xyp.sample.spring.webapi.domain.task.entity.batch;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.xyp.shared.id.generator.table.hibernate.CustomizedTableIdGenerator;
import org.xyp.shared.id.generator.table.hibernate.HibernateIdTableGeneratorLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Builder
@Entity
@Table(schema = "test", name = "batch")
public record BatchRecord(

    @SuppressWarnings("deprecation")
    @EmbeddedId
    @GeneratedValue(generator = Batch.BatchId.ID_NAME)
    @GenericGenerator(name = Batch.BatchId.ID_NAME, type = HibernateIdTableGeneratorLegacy.class)
    @CustomizedTableIdGenerator(name = Batch.BatchId.ID_NAME)
    @JsonUnwrapped
    Batch.BatchId id,

    @org.springframework.data.relational.core.mapping.Column("company_id")
    @Column(name = "company_id")
    int companyId,

    @org.springframework.data.relational.core.mapping.Column("batch_name")
    @Column(name = "batch_name")
    String batchName,

    @OneToMany(
        targetEntity = BatchRule.class,
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = Batch.BatchRef.COLUMN)
    List<BatchRule> batchRules
) {

    public BatchRecord {
        log.info("default constructor");
    }


    public BatchRecord() {
        this((Batch.BatchId) null, 0, null, new ArrayList<>());
        log.info("non-args constructor");
    }

    public BatchRecord(Long id, int companyId, String batchName) {
        this(
            null != id ? new Batch.BatchId(id) : null,
            companyId, batchName, new ArrayList<>()
        );
    }

    public void setBatchRules(List<BatchRule> batchRules) {
        if (null == batchRules) {
            return;
        }
        this.batchRules.clear();
        this.batchRules.addAll(batchRules);
    }

    ///////////////////////////////////////////

    @Override
    public String toString() {
        return "BatchRecord{" +
            "batchName='" + batchName + '\'' +
            ", companyId=" + companyId +
            ", id=" + id +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchRecord that = (BatchRecord) o;
        return companyId == that.companyId && Objects.equals(id, that.id) && Objects.equals(batchName, that.batchName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, batchName);
    }

}
