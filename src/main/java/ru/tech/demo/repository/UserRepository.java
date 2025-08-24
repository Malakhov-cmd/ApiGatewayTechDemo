package ru.tech.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tech.demo.entity.UserAccount;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}