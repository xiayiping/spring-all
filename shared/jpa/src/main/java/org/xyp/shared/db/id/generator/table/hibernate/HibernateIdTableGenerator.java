package org.xyp.shared.db.id.generator.table.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

@Slf4j
public class HibernateIdTableGenerator
    extends DefaultBeforeExecutionGeneratorImpl
    implements BeforeExecutionGenerator {

    public HibernateIdTableGenerator(
        CustomizedTableIdGenerator config,
        Member annotatedMember,
        CustomIdGeneratorCreationContext ignored
    ) {
        super(
            config.name(),
            config.datasource(),
            config.defaultFetchSize(),
            config.defaultStepSize(),
            ((Field) annotatedMember).getType()
        )
        ;
        log.info("create HibernateIdTableGenerator for {}", config);
    }

}
