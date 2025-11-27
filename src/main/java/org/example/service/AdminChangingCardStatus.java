package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.example.entity.Card;
import org.example.entity.Status;
import org.example.exception.InsufficientFundsException;
import org.example.repository.CardRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Optional;

@Service
public class AdminChangingCardStatus {
    private final List<CardStatusChangeHandler> handlers;
    private final CardRepository cardRepository;

    public AdminChangingCardStatus(List<CardStatusChangeHandler> handlers, CardRepository cardRepository) {
        this.cardRepository = cardRepository;
        this.handlers = handlers;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void changeCardStatus(Long cardId, Status newStatus) throws InsufficientFundsException {
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        if (!cardOptional.isPresent()) {
            throw new EntityNotFoundException("Карточка с указанным ID не найдена");
        }
        Card cards = cardOptional.get();

        for (CardStatusChangeHandler handler : handlers) {
            if(handler.canHandle(newStatus)) {
                handler.handle(cards, newStatus);
                break;
            }
        }
        cardRepository.save(cards);
    }
}
