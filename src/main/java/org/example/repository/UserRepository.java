package org.example.repository;

import org.example.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Long>{

    List<Users> findByNameAndSurnameAndPatronymic(String name, String surname,  String patronymic);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM users WHERE id = ?1")
    void deleteUserWithCascade(Long userId);

    Users findByPhoneNumber(String phoneNumber);
}
