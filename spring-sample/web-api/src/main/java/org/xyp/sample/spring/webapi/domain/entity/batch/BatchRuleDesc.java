package org.xyp.sample.spring.webapi.domain.entity.batch;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.xyp.sample.spring.db.id.generator.jpa.HibernateIdTableGenerator;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@org.springframework.data.relational.core.mapping.Table("batch_rule_desc")
//@Entity
//@Table(name = "batch_rule_desc")
@Embeddable // hibernate no id solution
public class BatchRuleDesc {


//    @SuppressWarnings("deprecation")
//    @JsonUnwrapped
//    @org.springframework.data.annotation.Id
//    @EmbeddedId
//    @GeneratedValue(generator = BatchRuleDescId.ID_NAME)
//    @GenericGenerator(name = BatchRuleDescId.ID_NAME, type = HibernateIdTableGenerator.class)
//    private BatchRuleDescId id;

    @Column
    private String description;

    @Embeddable
    public record BatchRuleDescId(
        @Column
        Long id
    ) {
        public static final String ID_NAME = "org.xyp.sample.spring.webapi.domain.entity.BatchRuleDesc";

        public static BatchRuleDesc.BatchRuleDescId of(long id) {
            return new BatchRuleDesc.BatchRuleDescId(id);
        }
    }
}
