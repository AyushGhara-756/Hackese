package org.jail.hackese.users.repository;

import org.jail.hackese.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    User findByEmail(String email);
    boolean existsByEmail(String email);
}
