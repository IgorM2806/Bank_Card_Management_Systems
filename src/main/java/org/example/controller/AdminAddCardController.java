package org.example.controller;

import org.example.dto.CardResponseDTO;
import org.example.dto.CreateCardDTO;
import org.example.entity.Card;
import org.example.exception.DuplicateCardNumberException;
import org.example.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/v1/addCards")
public class AdminAddCardController {

    private AdminService adminService;

    public AdminAddCardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<CardResponseDTO> addCardToUser(@PathVariable Long userId, @RequestBody CreateCardDTO dto) {
        validateCardData(dto);
        try {
            Card createCard = adminService.createCardForUser(userId, dto.getCardNumber(), dto.getExpirationDate(),
                    dto.getStatus(), dto.getBalance());

            CardResponseDTO responseDTO = convertToDTO(createCard);
            return ResponseEntity.ok(responseDTO);
        }catch (DuplicateCardNumberException e) {
            CardResponseDTO errorResponse = new CardResponseDTO();
            errorResponse.setErrorMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    private void validateCardData(CreateCardDTO dto) {
        if (dto.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Номер карты не может быть пустым.");
        }
        if (dto.getBalance() == null || dto.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Баланс карты должен быть положительным числом.");
        }
    }

    private CardResponseDTO convertToDTO(Card card) {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }
}
