package org.xyp.sample.spring.webapi.domain.task.entity.batch;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@org.springframework.data.relational.core.mapping.Table(schema = "test", name = "batch_rule_desc")
@Embeddable // hibernate no id solution
public class BatchRuleDesc {

    @org.springframework.data.relational.core.mapping.Column("description")
    @Column(name = "description")
    private String description;
}
