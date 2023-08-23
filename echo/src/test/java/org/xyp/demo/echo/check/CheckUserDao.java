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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest(classes = {EchoMainApp.class})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CheckUserDao {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Order(1)
    void saveUser() {
        val user = new User(-1L, "userZero");
        User saved = userRepository.save(user);
        Assertions.assertTrue(saved.getId() > 0);

        val retrieve = userRepository.abcabc(saved.getId());
        Assertions.assertEquals(saved, retrieve);
    }

    @Test
    @Order(2)
    void retrieveAllUsers() {
        val retrieve = userRepository.findAll();
        Assertions.assertTrue(!retrieve.isEmpty());
        assertThat(retrieve).isNotEmpty();
        assertThatThrownBy(() -> {
            val a = 1 / 0;
            System.out.println(a);
        }).isInstanceOf(RuntimeException.class)
                .hasNoCause();
    }

    @Test
    void testTemplate() {
        val t = new CheckTemplate<Integer>();
        t.add(1);
        CheckTemplate<? extends User> uTemplate = null;
        assertThat(t.get()).isEqualTo(1);

        SubUser u = new SubUser();
        User p = new User(1L, "");
        List<SubUser> users = List.of();
        List<User> usersp = List.of();
        List<Object> lobj = List.of();
//        CheckTemplate.setUser(u, users);

        CheckTemplate.setUser(u, users);

        CheckTemplate.setUser3(u, users);
        CheckTemplate.setUser3(p, users);
        List<User> r1 = CheckTemplate.setUser3(p, usersp);
        List<SubUser> r = CheckTemplate.setUser3(p, users);
//        CheckTemplate.setUser3(p, lobj);
    }

    static class SubUser extends User {

    }
}
