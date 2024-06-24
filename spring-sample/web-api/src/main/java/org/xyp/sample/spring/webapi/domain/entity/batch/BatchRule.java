package org.xyp.sample.spring.webapi.domain.entity.batch;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.stereotype.Component;
import org.xyp.sample.spring.db.id.domain.HasId;
import org.xyp.sample.spring.db.id.domain.IdWrapper;
import org.xyp.sample.spring.db.id.generator.jpa.HibernateIdTableGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@org.springframework.data.relational.core.mapping.Table(schema = "test", name = "batch_rule")
@Entity
@Table(schema = "test", name = "batch_rule")
public class BatchRule implements HasId<BatchRule, Long> {

    // JPA need an ID field
    @SuppressWarnings("deprecation")
    @JsonUnwrapped
    @org.springframework.data.annotation.Id
    @EmbeddedId
    @GeneratedValue(generator = BatchRuleId.ID_NAME)
    @GenericGenerator(name = BatchRuleId.ID_NAME, type = HibernateIdTableGenerator.class)
    private BatchRuleId id;

    @org.springframework.data.relational.core.mapping.Column("rule_name")
    @Column(name = "rule_name")
    private String ruleName;


    @MappedCollection(
        idColumn = "rule_id",
        keyColumn = "description"
    )
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "test", name = "batch_rule_desc", joinColumns = @JoinColumn(name = "rule_id"))
    private Set<BatchRuleDesc> batchRuleDescriptions;
    // don't need many to one here, because there's already @JoinColumn in @OneToMany side
    // user below for one to one embedded with fk
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "address_id")
//    @Embedded
/////////////////////////////////////////////////


    public BatchRule(BatchRuleId id, String ruleName) {
        this.id = id;
        this.ruleName = ruleName;
    }

    public BatchRule(Long id, String ruleName) {
        if (null != id)
            this.id = new BatchRuleId(id);
        this.ruleName = ruleName;
    }

    @Override
    public void refreshId(Long id) {
        this.id = BatchRuleId.of(id);
    }

    @Override
    public String identityGeneratorName() {
        return BatchRuleId.ID_NAME;
    }

    @Override
    public Long peekId() {
        return Optional.ofNullable(this.id)
            .map(i -> i.id)
            .filter(i -> i > 0)
            .orElse(null);
    }

    @Embeddable
    public record BatchRuleId(
        @Column
        Long id
    ) implements IdWrapper<Long> {
        public static final String ID_NAME = "org.xyp.sample.spring.webapi.domain.entity.BatchRule";

        public static BatchRuleId of(long id) {
            return new BatchRuleId(id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchRule batchRule = (BatchRule) o;
        return Objects.equals(id, batchRule.id) && Objects.equals(ruleName, batchRule.ruleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ruleName);
    }

    @Override
    public String toString() {
        return "BatchRule{" +
            "id=" + id +
            ", ruleName='" + ruleName + '\'' +
            '}';
    }

    @Component
    @ReadingConverter
    public static class BatchRuleIdReadingConverter implements Converter<Long, BatchRuleId> {
        @Override
        public BatchRuleId convert(Long source) {
            return new BatchRuleId(source);
        }
    }

    @Component
    @WritingConverter
    public static class BatchRuleIdWritingConverter implements Converter<IdWrapper<Long>, Long> {
        @Override
        public Long convert(IdWrapper<Long> source) {
            return source.id();
        }
    }

    public static void main(String[] args) {
        val b = Batch.builder()
            .batchName("1");
        System.out.println(b.batchName("2"));
        System.out.println(b.batchName("3"));
        System.out.println(b.batchName("4"));
    }
}


