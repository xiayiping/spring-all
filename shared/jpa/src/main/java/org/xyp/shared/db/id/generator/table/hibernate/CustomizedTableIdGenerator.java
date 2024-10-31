package org.xyp.shared.db.id.generator.table.hibernate;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(HibernateIdTableGenerator.class)
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface CustomizedTableIdGenerator {
    String name();

    String datasource() default "";

    int defaultFetchSize() default 50;

    int defaultStepSize() default 1;
}
