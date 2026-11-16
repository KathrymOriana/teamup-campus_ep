package com.campus.teamup.repository;

import java.util.Optional;

import com.campus.teamup.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
