package com.Shop.shop.repository;

import com.Shop.shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    void deleteByUsername(String username);
}
