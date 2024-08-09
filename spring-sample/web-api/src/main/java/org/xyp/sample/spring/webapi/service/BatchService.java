package org.xyp.sample.spring.webapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xyp.sample.spring.tracing.Trace;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.repository.jpa.BatchDaoJpa;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class BatchService {
    final BatchDaoJpa batchDaoJpa;

    @Transactional(rollbackFor = Exception.class)
    public Batch update(Long id, Batch batch) {

        log.info("update batch id:{}", id);
        batch.setId(Batch.BatchId.of(id));
        batch.getBatchRules().forEach(f -> f.setRuleName("ss " + System.currentTimeMillis()));

        val tmp = batchDaoJpa.findWithRulesById(batch.getId());
//        tmp.get().getBatchRules().clear();
//        tmp.get().getBatchRules().addAll(batch.getBatchRules());
//        val newTmp = batchDaoJpa.save(tmp.get());
//        newTmp.getBatchRules().forEach(r -> {}
//            log.debug("{}", Optional.ofNullable(r.getBatchRuleDescriptions()).map(l -> l.size()).orElse(0))
//        );
//        return newTmp;

        return tmp.map(bb -> {

            bb.setBatchName(UUID.randomUUID().toString());
            bb.setBatchRules(batch.getBatchRules());
            return batchDaoJpa.save(bb);
        }).orElse(null);
//        return batchDaoJpa.save(batch);
    }

    @Trace("batchService.dummy")
    public void dummy() {
        log.info("for future use");
    }
}
