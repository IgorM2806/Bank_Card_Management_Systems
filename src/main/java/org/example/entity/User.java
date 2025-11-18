package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
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
    private Set<Card> cards = new HashSet<>();

    // Новые поля для отслеживания состояния аккаунта
    @Column(name = "enabled", columnDefinition = "boolean default true")
    private boolean enabled = true;

    @Column(name = "locked", columnDefinition = "boolean default false")
    private boolean locked = false;

    @Column(name = "account_expired", columnDefinition = "boolean default false")
    private boolean accountExpired = false;

    @Column(name = "credentials_expired", columnDefinition = "boolean default false")
    private boolean credentialsExpired = false;

    public User() {}

    public User(String name, String surname, String patronymic, RoleEnum role, String passwordHash, String phoneNumber) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
    }

    public User(String phoneNumber, String passwordHash, RoleEnum role, boolean enabled, boolean accountExpired,
                boolean credentialsExpired, boolean locked) {
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.accountExpired = accountExpired;
        this.credentialsExpired = credentialsExpired;
        this.locked = locked;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Дополнительные методы для совместимости с Spring Security

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }
}

