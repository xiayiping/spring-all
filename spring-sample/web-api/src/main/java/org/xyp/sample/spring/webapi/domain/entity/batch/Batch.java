package org.xyp.sample.spring.webapi.domain.entity.batch;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 实践下来，spring DATA jdbc并不好用 <br/>
 * 1. strong typed id, 侵入式必须写writer/reader converter，<br/>
 * 2. aggregate reference必须要类本身自带 generic type parameter，即使父类有，自己没有也不行 <br/>
 * 3. aggregate leaf，不能有自己的id ，如必须要有，则只能为 List <br/>
 * 4. 深aggregate leaf 查询时我发现会有 ambiguous id 问题 (sql server express ,  postgre 即使重复id field也没问题)<br/>
 * 5. 默认不会为aggregate leaf生成 id ，即使已经定义了@Id 列
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
// 完整的object只会出现一次，且是第一次出现， 如果是第二次出现， 只会只有id reference
// 并不能解决所有问题， 如果第一次出现是在某个aggregate root的leaf中， 那么第二次即使是个跟高层级的Object ， 也只会变成ref
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@org.springframework.data.relational.core.mapping.Table(schema = "test", name = "batch")
@Entity
@Table(schema = "test", name = "batch")
@SqlResultSetMapping(
    name = "batch",
    classes = @ConstructorResult(
        targetClass = Batch.class,
        columns = {
            @ColumnResult(name = "company_id", type = Integer.class),
            @ColumnResult(name = "batch_name", type = String.class),
        }
    )
)
@DynamicUpdate
@DynamicInsert
public class Batch implements HasId<Long> {

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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    // have to be a 'List' for data-jdbc in order to force refresh the id
    // in data-jdbc the id for aggregate leaf is not needed, so have to do this tricky
    private List<BatchRule> batchRules;

    @PrePersist
    public void onPersist() {
        log.info("setup child rules refer to batch ......");
        // can also put something in to event collection for being used by @DomainEvents
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        log.info("fetch domain events ......");
        // return the event
        return List.of();
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        log.info("clear domain events ......");
//      clear the domain event :
    }

    @PersistenceCreator
    public Batch(Integer companyId, String batchName) {
        log.info("create using parameterized constructure");
        this.companyId = companyId;
        this.batchName = batchName;
    }

    @Override
    public void putGeneratedId(Long id) {
        this.id = BatchId.of(id);
    }

    @SuppressWarnings("unchecked")
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
