package org.xyp.shared.db.id.generator.table.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.MappingException;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.util.Optional;
import java.util.Properties;

/**
 *
 * <br/> https://hibernate.atlassian.net/jira/software/c/projects/HHH/issues/HHH-18276?jql=project%20%3D%20%22HHH%22%20ORDER%20BY%20created%20DESC
 * <br/> use legacy because for embedded id is not supported by new annotation and:
 * <br/> hibernate < 6.5, the GenericGenerator is not deprecated
 * <pre>
 * <code>
 * <span/> @GeneratedValue(generator = BatchId.ID_NAME)
 * <span/> @GenericGenerator(name = BatchId.ID_NAME, type = HibernateIdTableGeneratorLegacy.class, parameters = {
 * <span/>     @org.hibernate.annotations.Parameter(name = HibernateIdTableGeneratorLegacy.KEY_DEFAULT_FETCH_SIZE, value = "50"),
 * <span/>     @org.hibernate.annotations.Parameter(name = HibernateIdTableGeneratorLegacy.KEY_DEFAULT_STEP_SIZE, value = "2"),
 * <span/>     @org.hibernate.annotations.Parameter(name = HibernateIdTableGeneratorLegacy.KEY_DATASOURCE_NAME, value = "main"),
 * <span/> })
 * </code></pre>
 */
@Slf4j
public class HibernateIdTableGeneratorLegacy
    extends DefaultBeforeExecutionGeneratorImpl
    implements BeforeExecutionGenerator, Configurable {

    public HibernateIdTableGeneratorLegacy() {
        super("", "", null);
        log.info("create HibernateIdTableGeneratorLegacy ......");
    }

    private static final String BUILD_IN_GENERATOR_NAME = "GENERATOR_NAME";
    public static final String KEY_DEFAULT_FETCH_SIZE = "DEFAULT_FETCH_SIZE";
    public static final String KEY_DEFAULT_STEP_SIZE = "DEFAULT_STEP_SIZE";
    public static final String KEY_DATASOURCE_NAME = "DATASOURCE_NAME";

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        idClass = type.getReturnedClass();
        name = params.getProperty(BUILD_IN_GENERATOR_NAME);
        Optional.ofNullable(params.getProperty(KEY_DEFAULT_FETCH_SIZE)).map(Integer::valueOf)
            .ifPresent(this::setDefaultFetchSize);
        Optional.ofNullable(params.getProperty(KEY_DEFAULT_STEP_SIZE)).map(Integer::valueOf)
            .ifPresent(this::setDefaultStepSize);
        Optional.ofNullable(params.getProperty(KEY_DATASOURCE_NAME))
            .ifPresent(this::setDatasource);
        log.info(
            "config id generator for id class {}, default fetch size/step {} {}",
            idClass, defaultFetchSize, defaultStepSize
        );
    }
}
