package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.exception.CardNotFoundException;
import org.example.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class CardDeletionAdminService {

    private final CardRepository cardRepository;

    public CardDeletionAdminService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Удаляет карту по её идентификатору.
     *
     * @param cardId уникальный идентификатор карты.
     * @throws CardNotFoundException если карта с указанным идентификатором не найдена.
     */
    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteCard(Long cardId) {
        System.out.println("Сервис - CardDeletionAdminService!");
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Карта с указанным ID не найдена");
        }
        cardRepository.deleteById(cardId);
    }
}
