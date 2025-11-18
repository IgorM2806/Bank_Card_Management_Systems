package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardResponseDTO {
    private long id;
    private String cardNumber;
    private LocalDate expirationDate;
    private Status status;
    private BigDecimal balance;
    private String errorMessage;

    public CardResponseDTO(String cardNumber, LocalDate expirationDate, Status status, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }

    public CardResponseDTO() {}

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
}
