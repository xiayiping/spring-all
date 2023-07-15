package org.xyp.demo.domain;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/add")
    public DummyUser saveDummy() {
        DummyUser du = new DummyUser(100L, "dummy");
        return userService.save(du);
    }
}
