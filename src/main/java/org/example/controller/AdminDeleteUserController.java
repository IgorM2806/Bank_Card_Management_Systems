package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.exception.UserNotFoundException;
import org.example.service.UserDeletionAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/users")
public class AdminDeleteUserController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserDeletionAdminService userDeletionAdminService;

    public AdminDeleteUserController(UserDeletionAdminService userDeletionAdminService) {
        this.userDeletionAdminService = userDeletionAdminService;
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userDeletionAdminService.deleteUser(userId);
            return  ResponseEntity.noContent().build();
        }catch (UserNotFoundException e) {
            logger.error("Ошибка при удалении пользователя" + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
