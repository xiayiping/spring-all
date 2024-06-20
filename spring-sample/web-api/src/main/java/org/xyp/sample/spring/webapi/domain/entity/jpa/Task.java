package org.xyp.sample.spring.webapi.domain.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import org.xyp.sample.spring.db.id.IdValidatorLong;

import java.util.Objects;

@AllArgsConstructor
@Entity
@Table(schema = "KYC", name = "KycTask")
public class Task {
    @EmbeddedId
    private TaskId id;
    @Column(name = "companyId")
    final int companyId;
    @Column(name = "employeeId")
    final String employeeId;

    public Task() {
        this.companyId = 0;
        this.employeeId = null;
    }

    public static Task of(int companyId, String employeeId/*, Batch.BatchId batchId*/) {
        return new Task(null, companyId, employeeId/*, batchId.toAggregateReference()*/);
    }

    public static record TaskId(long id) {
        public TaskId {
            IdValidatorLong.validate(id);
        }

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

