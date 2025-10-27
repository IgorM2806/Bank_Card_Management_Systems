package org.example.controller;

import org.example.entity.Cards;
import org.example.service.CardTransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transferCards")
public class UserTransferController {
    private final CardTransferService cardTransferService;

    public UserTransferController(CardTransferService cardTransferService) {
        this.cardTransferService = cardTransferService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<Cards> getUserCards(@PathVariable Long userId) {
        return cardTransferService.getUserCards(userId);
    }
    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    public void transferMoneyBetweenCards(@RequestBody TransferData transferData) {
        cardTransferService.performTransferForUser(transferData.getUserId(),
                transferData.getSourceCard(), transferData.getTargetCard(), transferData.getAmount());


    }

    static class TransferData {
        private Long userId;
        private Long sourceCard;
        private Long targetCard;
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
