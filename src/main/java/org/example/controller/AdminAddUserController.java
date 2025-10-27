package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.CreateUserDTO;
import org.example.entity.User;
import org.example.exception.DuplicateUserException;
import org.example.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
    @RequestMapping("/admin/users")
    public class AdminAddUserController {
        @Autowired
        private AdminService adminService;

        @PostMapping("/add")
        public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO dto) {
            System.out.println("Контроллер - AdminAddUserController_createUser!");
            User createdUser = adminService.createUser(dto.getFirstName(),
                    dto.getSurname(), dto.getPatronymic(), dto.getRole(), dto.getPasswordHash(), dto.getPhoneNumber());
            return ResponseEntity.ok(createdUser);
        }

    @PostMapping("/password/update")
    public User updateUserPasswordHash(@RequestParam ("userId") Long userId,
                                       @RequestParam("password")  String password) {
        System.out.println("Пароль успешно обновлен!");
        return adminService.updateUserPassword(userId, password);
    }

        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public Map<String, Object> handleValidationErrors(MethodArgumentNotValidException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ошибка валидации.");
            response.put("errors", ex.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)));
            return response;
        }

        // Обработчик для дупликации пользователей
        @ResponseStatus(HttpStatus.CONFLICT)
        @ExceptionHandler(DuplicateUserException.class)
        public Map<String, Object> handleDuplicateUserException(DuplicateUserException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", ex.getMessage());
            response.put("exception", ex.getClass().getSimpleName());
            return response;
        }

        // Универсальный обработчик для всех остальных исключений
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler(Exception.class)
        public Map<String, Object> handleAllOtherExceptions(Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", ex.getMessage());
            response.put("exception", ex.getClass().getSimpleName());
            return response;
        }
}
