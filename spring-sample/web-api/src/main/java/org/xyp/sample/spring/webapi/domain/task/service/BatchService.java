package org.xyp.sample.spring.webapi.domain.task.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
//import org.xyp.sample.spring.tracing.Trace;
import org.xyp.sample.spring.webapi.domain.task.pojo.BatchPojo;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.BatchRule;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.BatchRuleDesc;
import org.xyp.sample.spring.webapi.domain.task.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.domain.shared.service.transactional.CommonTransactionalService;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class BatchService {

    final BatchDaoJpa batchDaoJpa;
    final TransactionTemplate transactionTemplate;
    final CommonTransactionalService commonTransactionalService;

    public BatchPojo update(BatchPojo batchPojo) {

        log.info("update batch :{}", batchPojo);

        val updated = commonTransactionalService.updateEntity(
            batchPojo.id(),
            batchDaoJpa::findWithRulesById,
            batch -> {
                batch.setBatchName(batchPojo.batchName());
                batch.setCompanyId(batchPojo.companyId());
                batch.getBatchRules().clear();
                batch.getBatchRules().addAll(
                    List.of(
                        getRandomBatchRule(batch),
                        getRandomBatchRule(batch)
                    )
                );
            },
            batchDaoJpa::save
        );

        return BatchPojo.from(updated);
    }

    public List<BatchPojo> updateBatchesRandomly() {

        log.info("update batches randomly ");

        val ids = Stream.of(351L, 352L, 353L)
            .map(Batch.BatchId::of)
            .toList();

        val dtoMap = new HashMap<Batch.BatchId, BatchPojo>();
        dtoMap.put(
            Batch.BatchId.of(1L), BatchPojo.builder().build()
        );

        val updated = commonTransactionalService.updateEntities(
            Batch::getId,
            batchDaoJpa::findWithRulesByIds,
            getId2UpdaterMapping(ids,
                id -> bt -> {
                    log.trace("{}", dtoMap.get(id));
                    bt.setBatchName("ss " + UUID.randomUUID());
                    val rules = List.of(
                        getRandomBatchRule(bt),
                        getRandomBatchRule(bt),
                        getRandomBatchRule(bt)
                    );
                    bt.getBatchRules().clear();
                    bt.getBatchRules().addAll(rules);
                }),
            batchDaoJpa::saveAll
        );

        return updated.stream()
            .map(BatchPojo::from)
            .toList();
    }

    private static <T, I> Map<I, Consumer<T>> getId2UpdaterMapping(
        List<I> ids,
        Function<I, Consumer<T>> updater
    ) {
        return ids.stream().map(id ->
            new AbstractMap.SimpleEntry<>(id, updater.apply(id))
        ).collect(Collectors.toMap(
            AbstractMap.SimpleEntry::getKey,
            AbstractMap.SimpleEntry::getValue
        ));
    }

    private static BatchRule getRandomBatchRule(Batch bt) {
        return BatchRule.builder()
            .ruleName("new name " + UUID.randomUUID())
            .batchRuleDescriptions(Set.of(
                BatchRuleDesc.builder()
                    .description("new desc " + UUID.randomUUID())
                    .build()
            )).build();
    }

//    @Trace("batchService.dummy")
    public void dummy() {
        log.info("for future use");
    }
}
