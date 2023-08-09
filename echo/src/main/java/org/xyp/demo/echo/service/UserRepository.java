package org.xyp.demo.echo.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xyp.demo.echo.pojo.User;

/**
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.collections-and-iterables <br/>
 * Some key points I didn't know before: <br/>
 * <ul>
 *     <li>Projections: query only last name for users</li>
 *     <li>@NamedStoredProcedureQuery</li>
 *     <li>Query by Example which is a technique of user-friendly</li>
 *     <li>Locking on @query, like READ lock</li>
 *     <li>Auditing, who change the data at when</li>
 * </ul>
 *
 */
@Repository
public interface UserRepository extends ListCrudRepository<User, Long> {

    @Query(value = "select id as id, name as name from user where id = :id", nativeQuery = true)
    public User abcabc(@Param("id") Long id);

    public User abcabc2(@Param("id") Long name);

}
