package org.xyp.demo.echo.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyp.demo.echo.pojo.User;

import java.util.Collection;

@Service
public class UserServiceDefault implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Collection<User> saveUsers(Collection<User> users) {
        return userRepository.saveAll(users);
    }

}
