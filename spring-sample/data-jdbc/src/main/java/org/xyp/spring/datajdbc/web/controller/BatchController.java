package org.xyp.spring.datajdbc.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xyp.spring.datajdbc.domain.pojo.Batch;
import org.xyp.spring.datajdbc.domain.repository.BatchRepository;
import org.xyp.spring.datajdbc.web.dto.BatchDto;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/batches")
public class BatchController {

    private final BatchRepository batchRepository;

    @GetMapping("/{id}")
    public Optional<BatchDto> get(@PathVariable Long id) {
        return batchRepository.get(Batch.Id.of(id))
            .map(BatchDto::of);
    }

    @PostMapping("")
    public BatchDto post(@RequestBody BatchDto batch) {
        return BatchDto.of(batchRepository.save(batch.toBatch()));
    }

    @PostMapping("/many")
    public List<BatchDto> postMany() {
        List<Batch> list = List.of(
            new Batch(null, "na"),
            new Batch(null, "nb"),
            new Batch(null, "nc")
        );
        return batchRepository.saveAll(list)
            .stream().map(BatchDto::of)
            .toList()
            ;
    }
}
