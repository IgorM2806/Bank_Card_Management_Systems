package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardBalanceDto {
    private String cardNumber;
    private BigDecimal balance;

    public  CardBalanceDto(String cardNumber, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.balance = balance;
    }
}
