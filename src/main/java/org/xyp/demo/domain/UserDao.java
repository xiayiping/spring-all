package org.xyp.demo.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  UserDao extends JpaRepository<DummyUser, Long> {
}
