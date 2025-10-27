package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.entity.RoleEnum;

@Data
public class CreateUserDTO {

    @NotNull(message = "Имя обязательно для заполнения.")
    @Size(min = 1, message = "Имя не должно быть пустой строкой.")
    private String firstName;

    @NotNull(message = "Фамилия обязательна для заполнения.")
    @Size(min = 1, message = "Фамилия не должна быть пустой строкой.")
    private String surname;

    @NotNull(message = "Отчество обязательно для заполнения.")
    @Size(min = 1, message = "Отчество не должно быть пустой строкой.")
    private String patronymic;

    @NotNull(message = "Роль пользователя обязательна для заполнения.")
    private RoleEnum role;

    private String passwordHash;

    @NotNull(message = "Телефон обязателен для заполнения.")
    @Size(min = 1, message = "Телефон не должен быть пустой строкой.")
    private String phoneNumber;

    public CreateUserDTO(String firstName, String surname, String patronymic,
                         RoleEnum role, String passwordHash, String phoneNumber) {
        this.firstName = firstName;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
    }
}

