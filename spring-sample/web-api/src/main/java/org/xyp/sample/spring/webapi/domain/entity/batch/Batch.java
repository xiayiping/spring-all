package org.xyp.sample.spring.webapi.domain.entity.batch;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.stereotype.Component;
import org.xyp.sample.spring.db.id.domain.HasId;
import org.xyp.sample.spring.db.id.generator.jpa.HibernateIdTableGenerator;

import java.util.*;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
// 并不能解决所有问题， 如果对方又在many side，又在本身List side 容易将最外层的变为ref
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@org.springframework.data.relational.core.mapping.Table("batch")
@Entity
@Table(name = "batch")
public class Batch implements HasId<Batch, Long> {

    @SuppressWarnings("deprecation")
    @org.springframework.data.annotation.Id
    @EmbeddedId
    @GeneratedValue(generator = BatchId.ID_NAME)
    @GenericGenerator(name = BatchId.ID_NAME, type = HibernateIdTableGenerator.class)
    @JsonUnwrapped
    private BatchId id;

    @org.springframework.data.relational.core.mapping.Column("company_id")
    @Column(name = "company_id")
    int companyId;

    @org.springframework.data.relational.core.mapping.Column("batch_name")
    @Column(name = "batch_name")
    String batchName;

    //    @JsonManagedReference
    @MappedCollection(
        idColumn = "batch_id",
        keyColumn = "id"
    )
    @OneToMany(
        targetEntity = BatchRule.class,
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = BatchRef.COLUMN)
    // have to be a 'List' for data-jdbc in order to force refresh the id
    // in data-jdbc the id for aggregate leaf is not needed, so have to do this tricky
    private List<BatchRule> batchRules;

    @PrePersist
    public void onPersist() {
//        if (null == tasks) {
//            return;
//        }
        log.info("setup child rules refer to batch ......");
//        if (null != batchRules) {
//            batchRules.forEach(r -> r.setBatch(this));
//        }
//        tasks.forEach(t -> t.setBatch(this));
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        log.info("fetch domain events ......");
        return List.of();
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        log.info("clear domain events ......");
//        domainEvents.clear();
    }

    @PersistenceCreator
    public Batch(int companyId, String batchName) {
        this.companyId = companyId;
        this.batchName = batchName;
    }

    @Override
    public void refreshId(Long id) {
        this.id = BatchId.of(id);
    }

    @Override
    public Collection<BatchRule> leaves() {
        return batchRules;
    }

    @Override
    public Long peekId() {
        return Optional.ofNullable(this.id)
            .map(i -> i.id)
            .filter(i -> i > 0)
            .orElse(null);
    }

    @Override
    public String identityGeneratorName() {
        return BatchId.ID_NAME;
    }

    @Embeddable
    public record BatchId(
        @Column
        Long id
    ) {
        public static final String ID_NAME = "org.xyp.sample.spring.webapi.domain.entity.Batch";

        public static BatchId of(long id) {
            return new BatchId(id);
        }
    }

    /**
     * T just for compatible with TypeDiscoverer in Data JDBC
     *
     * @param id
     * @param <T>
     */
    @Embeddable
    public record BatchRef<T>(
        @Column(name = COLUMN)
        Long id
    ) {
        public static final String COLUMN = "batch_id";

        public static BatchRef<Batch> of(Batch batch) {
            return new BatchRef<>(batch.getId().id);
        }
    }

    @Override
    public String toString() {
        return "Batch{" +
            "id=" + id +
            ", companyId=" + companyId +
            ", batchName='" + batchName + '\'' +
            ", batchRules=" + batchRules +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return companyId == batch.companyId && Objects.equals(id, batch.id) && Objects.equals(batchName, batch.batchName) && Objects.equals(batchRules, batch.batchRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, batchName, batchRules);
    }

    @Component
    @ReadingConverter
    public static class BatchIdReadingConverter implements Converter<Long, Batch.BatchId> {
        @Override
        public Batch.BatchId convert(Long source) {
            return new Batch.BatchId(source);
        }
    }

    @Component
    @WritingConverter
    public static class BatchIdWritingConverter implements Converter<Batch.BatchId, Long> {
        @Override
        public Long convert(Batch.BatchId source) {
            return source.id();
        }
    }

    @Component
    @ReadingConverter
    public static class BatchRefReadingConverter implements Converter<Long, BatchRef<Batch>> {
        @Override
        public Batch.BatchRef<Batch> convert(Long source) {
            return new Batch.BatchRef<>(source);
        }
    }

    @Component
    @WritingConverter
    public static class BatchRefWritingConverter implements Converter<BatchRef<Batch>, Long> {
        @Override
        public Long convert(Batch.BatchRef<Batch> source) {
            return source.id();
        }
    }

}
