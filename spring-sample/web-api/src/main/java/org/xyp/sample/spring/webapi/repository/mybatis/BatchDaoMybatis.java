package org.xyp.sample.spring.webapi.repository.mybatis;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRecord;

@Repository
@Mapper
public interface BatchDaoMybatis {

    @Select("""
        select batch.id, batch.company_id , batch.batch_name,
        r.id as batch_rule_id, r.rule_name as batch_rule_rule_name
        from test.batch batch
        left join test.batch_rule r  on r.batch_id = batch.id
        where batch.id = #{id.id}
        """)
    @ResultMap(value = "org.xyp.sample.spring.webapi.repository.mybatis.Mappers.BatchRecord")
    BatchRecord findById(@Param("id") Batch.BatchId id);
}
