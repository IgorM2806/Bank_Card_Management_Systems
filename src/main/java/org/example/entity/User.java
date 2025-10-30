package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column (name = "surname")
    private String surname;

    @Column(name = "patronymic", nullable = false)
    private String patronymic;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleEnum role;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "phone_number", nullable = false)
    @Size(min = 10, max = 10, message = "Телефонный номер должен содержать ровно 10 символов.")
    private String phoneNumber;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Cards> cards = new HashSet<>();

    public User() {}

    public User(String name, String surname, String patronymic, RoleEnum role, String passwordHash, String phoneNumber) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
    }
}
