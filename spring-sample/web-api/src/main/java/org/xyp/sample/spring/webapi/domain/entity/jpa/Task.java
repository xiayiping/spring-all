package org.xyp.sample.spring.webapi.domain.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.xyp.sample.spring.db.id.IdValidatorLong;
import org.xyp.sample.spring.db.id.domain.IdHolder;
import org.xyp.sample.spring.db.id.jpa.HibernateIdTableGenerator;

import java.util.Objects;

@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "task")
public class Task {

    // @IdGeneratorType not supported yet
    // @CustomSequence(name = "sss", target = TaskId.class)
    // https://hibernate.atlassian.net/jira/software/c/projects/HHH/issues/HHH-18276?jql=project%20%3D%20%22HHH%22%20ORDER%20BY%20created%20DESC
    @EmbeddedId
    @GeneratedValue(generator = "sss")
    @GenericGenerator(name = "sss", type = HibernateIdTableGenerator.class)
    private TaskId id;

    final int companyId;
    final String employeeId;

    public Task() {
        this.companyId = 0;
        this.employeeId = null;
    }

    public static Task of(int companyId, String employeeId/*, Batch.BatchId batchId*/) {
        return new Task(null, companyId, employeeId/*, batchId.toAggregateReference()*/);
    }

    @Embeddable
//    @Data
    public record TaskId(
        @Column
        long id
    ) {

        public TaskId {
            IdValidatorLong.validate(id);
        }
//        public TaskId(Long id) {
//            this.id = id;
//        }

//        public TaskId() {
//            this.id = null;
//        }

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
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return companyId == task.companyId && Objects.equals(id, task.id) && Objects.equals(employeeId, task.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, employeeId);
    }
}

