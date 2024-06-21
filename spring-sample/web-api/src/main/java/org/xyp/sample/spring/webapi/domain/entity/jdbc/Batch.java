package org.xyp.sample.spring.webapi.domain.entity.jdbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Component;
import org.xyp.sample.spring.db.id.IdValidatorLong;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@Table(name = "batch")
public class Batch {

    @Id
    private BatchId id;

    @Column("company_id")
    final int companyId;

    @Column("batch_name")
    final String batchName;

    @MappedCollection(keyColumn = "batch_id", idColumn = "id")
    private Set<Task> tasks;

    public static record BatchId(
        long id
    ) {

        public BatchId {
            IdValidatorLong.validate(id);
        }

        public AggregateReference<Batch, Long> toAggregateReference() {
            return AggregateReference.to(id);
        }

        public static BatchId of(long id) {
            return new BatchId(id);
        }

        @Component
        @ReadingConverter
        public static class BatchIdReadingConverter implements Converter<Long, BatchId> {
            @Override
            public BatchId convert(Long source) {
                return new BatchId(source);
            }
        }

        @Component
        @WritingConverter
        public static class BatchIdWritingConverter implements Converter<BatchId, Long> {
            @Override
            public Long convert(BatchId source) {
                return source.id();
            }
        }
    }
}
