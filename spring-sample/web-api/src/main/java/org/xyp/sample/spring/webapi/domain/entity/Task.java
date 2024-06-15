package org.xyp.sample.spring.webapi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.xyp.sample.spring.db.id.entity.IdHolder;

@Data
@AllArgsConstructor
@Table(schema = "KYC", name = "KycTask")
public class Task implements IdHolder<Long, Task> {
    @Id
    long id;
    @Column("companyId")
    final int companyId;
    @Column("employeeId")
    final String employeeId;

    @Override
    public Task withId(Long id) {
        this.id = id;
        return this;
    }

    public static Task of(int companyId, String employeeId) {
        return new Task(0, companyId, employeeId);
    }
}
