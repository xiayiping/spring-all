package org.xyp.sample.spring.webapi.test.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    config = @SqlConfig(
        errorMode = SqlConfig.ErrorMode.CONTINUE_ON_ERROR
    ),
    scripts = {
        "classpath:/sql/schema.sql",
        "classpath:/sql/schema-mssql.sql",
    })
@SpringBootTest
@ActiveProfiles("test")
class TestJdbcRepo {

}
