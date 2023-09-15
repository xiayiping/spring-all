package org.xyp.demo.echo.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.xyp.demo.echo.pojo.MyIdTableGenerator.*;
import static org.xyp.demo.echo.pojo.User.*;


@TableGenerator(
    name = ID_SEQUENCE_NAME,
    table = TABLE,
    pkColumnName = PK_COLUMN,
    valueColumnName = VALUE_COLUMN,
    pkColumnValue = ID_SEQUENCE_NAME,
    allocationSize = 64
)

//
//@SqlResultSetMapping(
//    name = MAPPER_CONSTRUCTOR,
//    classes = {
//        @ConstructorResult(
//            targetClass = User.class,
//            columns = {
//                @ColumnResult(name = "id"),
//                @ColumnResult(name = "name")
//            }
//        )
//    }
//)
@SqlResultSetMapping(
    name = MAPPER_FIELD,
    entities = {
        @EntityResult(
            entityClass = User.class,
            fields = {
                @FieldResult(name = "id", column = "pp"),
                @FieldResult(name = "name", column = "nn")
            }
        )
    }
)
@NamedNativeQuery(name = "User.abcabc2", query = "select id as pp, name as nn from platform_user where id = :id",
    resultSetMapping = MAPPER_FIELD)
@Entity(name = "PlatformUser")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    public static final String ID_SEQUENCE_NAME = "UserIdOneByOneGenerator";
    public static final String MAPPER_CONSTRUCTOR = "UserMapConstructor";
    public static final String MAPPER_FIELD = "UserMapField";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_SEQUENCE_NAME)
    Long id;

    @Column
    private String name;
}
