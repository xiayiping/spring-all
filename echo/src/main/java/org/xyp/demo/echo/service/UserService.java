package org.xyp.demo.echo.service;

import org.xyp.demo.echo.pojo.User;

import java.util.Collection;

public interface UserService {

    User saveUser(User user);
    Collection<User> saveUsers(Collection<User> users);
}
