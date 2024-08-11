package org.xyp.sample.spring.webapi.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.*;
import org.xyp.sample.spring.tracing.Trace;
import org.xyp.sample.spring.webapi.domain.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.entity.task.Task;
import org.xyp.sample.spring.webapi.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.repository.jpa.TaskDaoJpa;
import org.xyp.sample.spring.webapi.service.BatchService;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/")
public class BatchController {

    final BatchDaoJpa batchDaoJpa;
    final TaskDaoJpa taskDaoJpa;
    final BatchService batchService;

    @PatchMapping("/batch/{id}")
    public Batch patch(@PathVariable("id") Long id, @RequestParam("name") String name) {
        val origin = batchDaoJpa.findWithRulesById(Batch.BatchId.of(id));
        return origin.map(
            o -> {
                o.setBatchName(name);
                return batchDaoJpa.save(o);
            }
        ).orElseThrow();
    }

    @PostMapping("/batch")
    public Batch batch(@RequestBody Batch batch) {
        return batchDaoJpa.save(batch);
    }

    @PutMapping("/batch/{id}")
    public Batch batchUpdate(@PathVariable Long id, @RequestBody Batch batch) {
        return batchService.update(id, batch);
    }

    @PostMapping("/task")
    public Task task(@RequestBody Task task) {
        return taskDaoJpa.save(task);
    }

    @Trace(value = "batch.get")
    @GetMapping("/batch/{id}")
    public Batch batch(@PathVariable Long id) {
        batchService.dummy();
        return batchDaoJpa.findWithRulesById(Batch.BatchId.of(id)).get();
    }

    @GetMapping("/task/{id}")
    public Task task(@PathVariable Long id) {
        log.info("find task with batch by id ");
        val task = taskDaoJpa.findWithBatchById(Task.TaskId.of(id)).get();
//        log.info("find task by id ");
//        return taskDaoJpa.findById(Task.TaskId.of(id)).get();

        log.info("set task batch contains no ref back");
        return task;
    }

    @PutMapping("/task/{id}")
    public Task putTask(@RequestBody Task task) {
        return taskDaoJpa.save(task);
    }

    @GetMapping("/tasks")
    public List<Task> tasks(@RequestParam List<Long> ids) {
        val taskIds = ids.stream().map(Task.TaskId::of).toList();
        val tasks = taskDaoJpa.findByIdIn(taskIds);

        log.info("set tasks batch contains no ref back");
        return tasks;
    }
}
