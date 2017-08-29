package io.loli.maikaze.repository;

import io.loli.maikaze.domains.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findByEmail(String email);

    public User findByUserName(String userName);

    User findByEmailOrUserName(String email, String name);
}