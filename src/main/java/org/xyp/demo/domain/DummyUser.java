package org.xyp.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.IdGeneratorType;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DummyUser {

    @Id
    private Long id;

    @Column
    String name;

    public DummyUser(String name) {
        this.name = name;
    }

}
