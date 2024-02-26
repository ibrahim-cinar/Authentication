package com.cinar.authentication.repository;

import com.cinar.authentication.model.User;
import org.springframework.context.annotation.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface  UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

}
