package org.xyp.shared.db.datasource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JpaProperties {

    List<String> jpaEntityBasePackages;
    String mybatisConfig;
    String hibernateDialect;
    Integer hibernateBatchFetchSize;
    Integer hibernateJdbcBatchSize;
    Boolean hibernateShowSql;
    Boolean hibernateGenerateStatistics;
    HibernateToDdlEnum hibernateToDdl = HibernateToDdlEnum.Validate;
    /**
     * The PhysicalNamingStrategy defines how Hibernate maps logical names (names defined in your entity classes or those derived by the ImplicitNamingStrategy)
     * to physical names in the database (i.e.; actual table and column names).
     */
    String hibernatePhysicalStrategy = PhysicalNamingStrategyStandardImpl.class.getName();
    /**
     * The ImplicitNamingStrategy is used when you do not explicitly specify a database name (such as a table or column name)
     * in your entity mappings (i.e.; when you don’t use the @Table or @Column annotations with explicit names).
     */
    String hibernateImplicitStrategy = SpringImplicitNamingStrategy.class.getName();
}
/*

 */