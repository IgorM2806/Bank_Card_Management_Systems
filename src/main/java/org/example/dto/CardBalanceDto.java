package org.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CardBalanceDto {
    private String cardNumber;
    private BigDecimal balance;

    public  CardBalanceDto(String cardNumber, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.balance = balance;
    }
}
