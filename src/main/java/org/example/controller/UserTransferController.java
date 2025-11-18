package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.entity.Card;
import org.example.service.CardTransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/v1/transferCards")
public class UserTransferController {
    private final CardTransferService cardTransferService;

    public UserTransferController(CardTransferService cardTransferService) {
        this.cardTransferService = cardTransferService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<Card> getUserCards(@PathVariable Long userId) {
        return cardTransferService.getUserCards(userId);
    }

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    public void transferMoneyBetweenCards(@Valid @RequestBody TransferData transferData) {
        System.out.println("transferMoneyBetweenCards: " + transferData.getUserId() + " / "
                + transferData.getSourceCard() + " / "
                + transferData.getTargetCard() + " / "
                + transferData.getAmount());
        cardTransferService.performTransferForUser(transferData.getUserId(),
                transferData.getSourceCard(), transferData.getTargetCard(), transferData.getAmount());
    }

    static class TransferData {
        @NotNull(message = "User ID is required")
        private Long userId;
        @NotNull(message = "Source card is required")
        private Long sourceCard;
        @NotNull(message = "Target card is required")
        private Long targetCard;
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        // Геттеры и сеттеры
        public Long getUserId() {return userId;}

        public void setUserId(Long userId) {this.userId = userId;}

        public Long getSourceCard() {return sourceCard;}

        public void setSourceCard(Long sourceCard) {this.sourceCard = sourceCard;}

        public Long getTargetCard() {return targetCard;}

        public void setTargetCard(Long targetCard) {this.targetCard = targetCard;}

        public BigDecimal getAmount() {return amount;}

        public void setAmount(BigDecimal amount) {this.amount = amount;}
    }
}
