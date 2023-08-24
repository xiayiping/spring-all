package org.xyp.demo.domain;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceJpa implements UserService {

    @Autowired
    UserDao userDao;

    @Override
    public DummyUser save(DummyUser user) {
        return userDao.save(user);
    }
}
