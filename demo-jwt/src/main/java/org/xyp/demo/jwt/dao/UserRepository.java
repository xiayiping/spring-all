package org.xyp.demo.jwt.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.xyp.demo.jwt.pojo.User;

import java.util.Optional;

@Service
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
