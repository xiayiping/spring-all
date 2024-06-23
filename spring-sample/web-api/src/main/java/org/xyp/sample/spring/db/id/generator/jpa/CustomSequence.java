package org.xyp.sample.spring.db.id.generator.jpa;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(HibernateIdTableGenerator.class)
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface CustomSequence {
    String name();

    int startWith() default 1;

    int incrementBy() default 50;

    Class<?> target();
}

