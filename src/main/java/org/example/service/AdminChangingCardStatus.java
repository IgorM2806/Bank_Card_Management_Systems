package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.example.entity.Card;
import org.example.entity.Status;
import org.example.exception.InsufficientFundsException;
import org.example.repository.CardRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminChangingCardStatus {

    private final CardRepository cardRepository;

    public AdminChangingCardStatus(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void changeCardStatus(Long cardId, Status newStatus) throws InsufficientFundsException {
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        if (!cardOptional.isPresent()) {
            throw new EntityNotFoundException("Карточка с указанным ID не найдена");
        }
        Card cards = cardOptional.get();
        cards.setStatus(newStatus);
        cardRepository.save(cards);
    }
}
