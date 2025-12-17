package com.analyfy.analify.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analyfy.analify.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMail(String mail);
}
