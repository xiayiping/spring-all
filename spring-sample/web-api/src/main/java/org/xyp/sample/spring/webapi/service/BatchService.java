package org.xyp.sample.spring.webapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.xyp.sample.spring.tracing.Trace;
import org.xyp.sample.spring.webapi.domain.dto.BatchDto;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRule;
import org.xyp.sample.spring.webapi.domain.entity.batch.BatchRuleDesc;
import org.xyp.sample.spring.webapi.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.service.transactional.TransactionalService;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class BatchService {

    final BatchDaoJpa batchDaoJpa;
    final TransactionTemplate transactionTemplate;
    final TransactionalService transactionalService;

    public BatchDto update(BatchDto batchDto) {

        log.info("update batch :{}", batchDto);

        val updated = transactionalService.updateEntity(
            batchDto.id(),
            batchDaoJpa::findWithRulesById,
            batch -> {
                batch.setBatchName(batchDto.batchName());
                batch.setCompanyId(batchDto.companyId());
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

        return BatchDto.from(updated);
    }

    public List<BatchDto> updateBatchesRandomly() {

        log.info("update batches randomly ");

        val ids = Stream.of(351L, 352L, 353L)
            .map(Batch.BatchId::of)
            .toList();

        val dtoMap = new HashMap<Batch.BatchId, BatchDto>();
        dtoMap.put(
            Batch.BatchId.of(1L), BatchDto.builder().build()
        );

        val updated = transactionalService.updateEntities(
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
            .map(BatchDto::from)
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

    @Trace("batchService.dummy")
    public void dummy() {
        log.info("for future use");
    }
}
