package com.cinar.authentication.repository;

import com.cinar.authentication.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface  UserRepository extends JpaRepository<User, String> {

    /*Optional<User> findByUsername(String username);*/
    Optional<User> findUserByEmail(String email);
   /* boolean existsById(@NotNull String username);*/

    @Transactional
    void deleteUserByEmail(String email);


}
