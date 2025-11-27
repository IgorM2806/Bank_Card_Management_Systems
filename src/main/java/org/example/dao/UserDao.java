package org.example.dao;

import org.example.entity.User;

import java.util.Optional;

public interface UserDao {

    boolean checkIfUserExistsByPhoneNumber(String phoneNumber);

    Optional<User> findUserById(Long id);
}