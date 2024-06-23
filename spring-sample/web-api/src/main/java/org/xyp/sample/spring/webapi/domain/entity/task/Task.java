package org.xyp.sample.spring.webapi.domain.entity.task;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import org.xyp.sample.spring.db.id.domain.HasId;
import org.xyp.sample.spring.db.id.generator.jpa.HibernateIdTableGenerator;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.springframework.core.convert.converter.Converter;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Getter
@Setter
// 完整的object只会出现一次， 只会只有id reference
// 并不能解决所有问题， 如果对方又在many side，又在本身List side 容易将最外层的变为ref
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@org.springframework.data.relational.core.mapping.Table("task")
@Entity
@Table(name = "task")
public class Task implements HasId<Task, Long> {

    // @IdGeneratorType not supported yet
    // @CustomSequence(name = "sss", target = TaskId.class)
    // https://hibernate.atlassian.net/jira/software/c/projects/HHH/issues/HHH-18276?jql=project%20%3D%20%22HHH%22%20ORDER%20BY%20created%20DESC
    @SuppressWarnings("deprecation")
    @org.springframework.data.annotation.Id
    // unwrap the id to flatten to parent object
    @JsonUnwrapped
    @EmbeddedId
    @GeneratedValue(generator = TaskId.ID_NAME)
    @GenericGenerator(name = TaskId.ID_NAME, type = HibernateIdTableGenerator.class)
    private TaskId id;

    @org.springframework.data.relational.core.mapping.Column("company_id")
    @Column(name = "company_id")
    private final Integer companyId;

    @org.springframework.data.relational.core.mapping.Column("employee_id")
    @Column(name = "employee_id")
    private final String employeeId;

    @org.springframework.data.relational.core.mapping.Column(Batch.BatchRef.COLUMN)
    private Batch.BatchRef<Batch> batch;

    public Task() {
        this.companyId = 0;
        this.employeeId = null;
    }

    public Task(TaskId id) {
        this();
        this.id = id;
    }
    // https://docs.spring.io/spring-data/jdbc/docs/3.1.7/reference/html/
    // see #ObjectCreation, jdbc use all args constructor only for record
    @PersistenceCreator
    public static Task of(int companyId, String employeeId, Batch.BatchRef<Batch> batch) {
        return new Task(null, companyId, employeeId, batch);
    }

    @Override
    public void refreshId(Long id) {
        this.id = TaskId.of(id);
    }

    @Override
    public String identityGeneratorName() {
        return TaskId.ID_NAME;
    }

    @Override
    public Long peekId() {
        return Optional.ofNullable(this.id)
            .map(i -> i.id)
            .filter(i -> i > 0)
            .orElse(null);
    }

    @Embeddable
    public record TaskId(
        @Column
        Long id
    ) {
        public static final String ID_NAME = "org.xyp.sample.spring.webapi.domain.entity.Task";

        public static TaskId of(Long id) {
            return new TaskId(id);
        }
    }

    @Override
    public String toString() {
        return "Task{" +
            "id=" + id +
            ", companyId=" + companyId +
            ", employeeId='" + employeeId + '\'' +
            ", batch=" + batch.id() +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return companyId == task.companyId && Objects.equals(id, task.id)
            && Objects.equals(employeeId, task.employeeId)
            && Objects.equals(batch, task.batch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, employeeId, batch.id());
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

