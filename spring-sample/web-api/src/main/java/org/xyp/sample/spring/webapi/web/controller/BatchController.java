package org.xyp.sample.spring.webapi.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.xyp.sample.spring.tracing.Trace;
import org.xyp.sample.spring.webapi.domain.task.pojo.BatchPojo;
import org.xyp.sample.spring.webapi.domain.task.entity.batch.Batch;
import org.xyp.sample.spring.webapi.domain.task.entity.task.Task;
import org.xyp.sample.spring.webapi.domain.task.repository.jpa.BatchDaoJpa;
import org.xyp.sample.spring.webapi.domain.task.repository.jpa.TaskDaoJpa;
import org.xyp.sample.spring.webapi.domain.task.service.BatchService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/")
public class BatchController {

    final BatchDaoJpa batchDaoJpa;
    final TaskDaoJpa taskDaoJpa;
    final BatchService batchService;

    @PostMapping("/say")
    public String batch(@RequestParam("say") String batch) {
        return batch;
    }

    @PostMapping("/batch")
    public Batch batch(@RequestBody Batch batch) {
        return batchDaoJpa.save(batch);
    }

    @PutMapping("/batch/{id}")
    public BatchPojo batchUpdate(@PathVariable Long id, @RequestBody BatchPojo batchdto) {
        if (!id.equals(batchdto.id().id())) {
            throw new IllegalArgumentException("id must be equal to id");
        }
        return batchService.update(batchdto);
    }

    @PutMapping("/batchs")
    public List<BatchPojo> batchesUpdate() {
        return batchService.updateBatchesRandomly();
    }

    @PostMapping("/task")
    public Task task(@RequestBody Task task) {
        return taskDaoJpa.save(task);
    }

    @PatchMapping("/batch{id}")
    public Batch patch(@PathVariable long id, @RequestParam("name") String name) {
        return batchDaoJpa.findById(Batch.BatchId.of(id))
            .map(b -> b.setBatchName(name))
            .map(batchDaoJpa::save)
            .orElse(null);
    }

    @Trace(value = "batch.get")
    @GetMapping("/batch/{id}")
    public EntityModel<Batch> batch(@PathVariable Long id) {
        batchService.dummy();
        val afford = afford(methodOn(HelloController.class).hello(null));
        val link = linkTo(methodOn(BatchController.class).batch(id)).withSelfRel();
        val linkPut = linkTo(methodOn(BatchController.class).batchUpdate(id, null))
            .withRel("update_key") // it's the key name
            .withType(HttpMethod.PUT.name())
            .withName("update_key_name")
            .withAffordances(List.of(afford));
        val batch = batchDaoJpa.findWithRulesById(Batch.BatchId.of(id)).get();
        return EntityModel.of(batch, link, linkPut);
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
