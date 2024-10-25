package org.xyp.spring.datajdbc.domain.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.xyp.spring.datajdbc.domain.pojo.Batch;

import java.util.List;

@Slf4j
@SpringBootTest
//@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class RepositoryTest {
    @Autowired
    BatchRepository batchRepository;

    Batch.Id id;

    @Test
    @Order(0)
    void testSave() {
        List<Batch> list = List.of(
            new Batch(null, "na", "d1"),
            new Batch(null, "nb", "d2"),
            new Batch(null, "nc", "d3")
        );
        val saved =
            batchRepository.saveAll(list)
            ;
        saved.forEach(System.out::println);

        id = saved.stream().findAny().map(Batch::id).orElse(null);
    }

    @Test
    @Order(10)
    void testGet() {
        System.out.println(id);
        System.out.println(batchRepository.get(id));

        batchRepository.get(id).ifPresent(bt-> {
            val saved = batchRepository.save(new Batch(bt.id(), bt.name(), "ssssss"));
            System.out.println(saved);
            Assertions.assertEquals(saved.id(), id);
        });

    }
}
