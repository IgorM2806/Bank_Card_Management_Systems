package org.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateCardDTO {
    private String cardNumber;
    private LocalDate expirationDate;
    private Status status;
    private BigDecimal balance;


    public CreateCardDTO(String cardNumber, LocalDate expirationDate, Status status, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }
}
