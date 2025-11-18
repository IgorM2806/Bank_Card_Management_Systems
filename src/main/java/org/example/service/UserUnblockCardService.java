package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.Card;
import org.example.entity.RequestBlocking;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
public class UserUnblockCardService {
    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    public UserUnblockCardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageResponse requestUnBlockCard(Long userId, Long cardId) throws AccessDeniedException {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) { // Используем isPresent() для ясности
            User user = userOpt.get();
            List<Card> userCards = cardRepository.findAllByOwner(user);

            boolean hasAccess = userCards.stream()
                    .anyMatch(c -> c.getId().equals(cardId));

            if (!hasAccess) {
                throw new AccessDeniedException("Карточка не принадлежит данному пользователю");
            }
            Optional<Card> currentCardOpt = cardRepository.findById(cardId);
            Card currentCard = currentCardOpt.get();
            if (currentCard.getRequestBlocking() == RequestBlocking.NO) {
                return new MessageResponse("Карточка Не заблокирована", 400);
            }

            int updatedRows = cardRepository.updateRequestBlocking("NO", cardId, userId);
            if (updatedRows != 1) {
                throw new RuntimeException("Не удалось отправить запрос на Разблокировку.");
            }
            return new MessageResponse("Карточка успешно разблокирована!", 200);

        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

}
