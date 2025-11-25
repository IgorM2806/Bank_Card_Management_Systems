package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.Card;
import org.example.entity.RequestBlocking;
import org.example.entity.User;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Service
public class UserRequestCardBlockService {
    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    public UserRequestCardBlockService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageResponse requestBlockCard(Long userId, Long cardId) throws AccessDeniedException {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            // Найдем нужную карту напрямую
            Optional<Card> currentCardOpt = cardRepository.findById(cardId);

            if (currentCardOpt.isEmpty()) {
                return new MessageResponse("Карточка не найдена!", 404);
            }

            Card currentCard = currentCardOpt.get();

            // Проверяем, совпадает ли владелец карты с указанным пользователем
            if (!currentCard.getOwner().getId().equals(userId)) {
                return new MessageResponse("Карточка принадлежит другому пользователю!", 403);
            }

            if (currentCard.getRequestBlocking() == RequestBlocking.YES) {
                return new MessageResponse("Запрос на блокировку уже отправлен для этой карты!", 400);
            }
            // Блокируем карту
            int updatedRows = cardRepository.updateRequestBlocking("YES", cardId, userId);
            if (updatedRows != 1) {
                throw new RuntimeException("Не удалось отправить запрос на блокировку.");
            }

            return new MessageResponse("Successfully requested to block the card.", 200);
        } else {
            return new MessageResponse("The user has not been found!", 404);
        }
    }
}
