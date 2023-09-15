package org.xyp.demo.echo.check;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.xyp.demo.echo.EchoMainApp;
import org.xyp.demo.echo.pojo.User;
import org.xyp.demo.echo.service.UserRepository;
import org.xyp.demo.echo.service.UserService;

import java.util.stream.Stream;

@Slf4j
@SpringBootTest(classes = {EchoMainApp.class})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CheckJpa {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository repository;

    @Test
    void testCreate() {
        Assertions.assertTrue(true);
    }

    @Test
    @Order(1)
    void testUserService() {
        User user = new User(-1L, "xyp");
        val user2 = userService.saveUser(user);
        Assertions.assertNotEquals(user, user2);
        Assertions.assertNotSame(user, user2);
    }

    @Test
    @Order(2)
    void testSaveUsers() {
        val list = Stream.iterate(1, i -> i + 1)
            .map(i -> new User((long) i, "xyp" + (System.currentTimeMillis())))
            .limit(100)
            .toList();

        val list2 = userService.saveUsers(list);
        Assertions.assertEquals(list.size(), list2.size());
    }

    @Test
    @Order(3)
    void testSaveUsersOneByOne() {
        val result = Stream.iterate(1, i -> i + 1)
            .map(i -> new User((long) i, "xyp" + (System.currentTimeMillis())))
            .limit(100)
            .map(u -> {
                log.error("save user: {}", u);
                return userService.saveUser(u);
            }).toList();
        log.error("----------------------");
        Assertions.assertEquals(100, result.size());
    }

    @Test
    @Order(4)
    void testGetUser() {
        val user = repository.abcabc(1L);
        Assertions.assertNotNull(user);
    }

    @Test
    @Order(5)
    void testGetUser2() {
        val user = repository.abcabc2(1L);
        Assertions.assertNotNull(user);
    }
}
