package org.example.controller;

import org.example.dto.CreateCardDTO;
import org.example.entity.Cards;
import org.example.exception.DuplicateCardNumberException;
import org.example.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/cards")
public class AdminAddCardController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> addCardToUser(@PathVariable Long userId, @RequestBody CreateCardDTO dto) {
        try {
            Cards createCard = adminService.createCardForUser(userId, dto.getCardNumber(), dto.getExpirationDate(),
                    dto.getStatus(), dto.getBalance());
            return ResponseEntity.ok(createCard);
        }catch (DuplicateCardNumberException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
