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
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Component;
import org.xyp.sample.spring.db.id.domain.IdHolder;
import org.xyp.sample.spring.db.id.IdValidatorLong;

@Data
@Builder
@AllArgsConstructor
@Table(schema = "KYC", name = "KycTask")
public class Task implements IdHolder<Long, Task> {
    @Id
    TaskId id;
    @Column("companyId")
    final int companyId;
    @Column("employeeId")
    final String employeeId;
    @Column("batchId")
    final AggregateReference<Batch, Long> batch;


    @Override
    public Task withId(Long id) {
        this.id = new TaskId(id);
        return this;
    }

    public static Task of(int companyId, String employeeId, Batch.BatchId batchId) {
        return new Task(null, companyId, employeeId, batchId.toAggregateReference());
    }

    public static record TaskId(
        long id
    ) {

        public TaskId {
            IdValidatorLong.validate(id);
        }

        @Component
        @ReadingConverter
        public static class TaskIdReadingConverter implements Converter<Long, TaskId> {
            @Override
            public TaskId convert(Long source) {
                return new TaskId(source);
            }
        }

        @Component
        @WritingConverter
        public static class TaskIdWritingConverter implements Converter<TaskId, Long> {
            @Override
            public Long convert(TaskId source) {
                return source.id();
            }
        }
    }
}
