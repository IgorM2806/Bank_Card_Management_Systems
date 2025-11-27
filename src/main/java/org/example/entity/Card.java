package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "card_number", nullable = false)
    @Size(min = 15, max = 16, message = "Номер карты должен быть ровно 16 символов.")
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private User owner;

    @Column (name = "expiration_date",nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestBlocking requestBlocking;

    @Column(name = "is_suspicious", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean suspicious;

    public Card() {}

    public Card(String cardNumber, LocalDate expirationDate, User owner, Status status,
                BigDecimal balance, RequestBlocking requestBlocking) {
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.owner = owner;
        this.status = status;
        this.balance = balance;
        this.requestBlocking = requestBlocking;
    }

    public Card(Long id) {
        this.id = id;
    }

    public boolean isSuspicious() {
        return suspicious;
    }

    @Override
    public String toString() {
        return "Cards{" +
                "id=" + id +
                ", cardNumber='" + cardNumber + '\'' +
                ", expirationDate=" + expirationDate +
                ", owner=" + owner +
                ", status=" + status +
                ", balance=" + balance +
                ", requestBlocking=" + requestBlocking +
                ", suspicious= "  + suspicious +
        '}';
    }
}
