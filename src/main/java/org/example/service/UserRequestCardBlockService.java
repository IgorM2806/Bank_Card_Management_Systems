package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.entity.Cards;
import org.example.entity.RequestBlocking;
import org.example.entity.Users;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserRequestCardBlockService {
    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    @Transactional
    public MessageResponse requestBlockCard(Long userId, Long cardId) throws AccessDeniedException {
        Optional<Users> userOpt = userRepository.findById(userId);
        System.out.println("requestBlockCard - userOpt.isPresent: " + userOpt.isPresent());

        if (userOpt.isPresent()) {
            Users user = userOpt.get();

            // Найдем нужную карту напрямую
            Optional<Cards> currentCardOpt = cardRepository.findById(cardId);

            if (currentCardOpt.isEmpty()) {
                return new MessageResponse("Карточка не найдена!", 404);
            }

            Cards currentCard = currentCardOpt.get();

            // Проверяем, совпадает ли владелец карты с указанным пользователем
            if (!currentCard.getOwner().getId().equals(userId)) {
                return new MessageResponse("Карточка принадлежит другому пользователю!", 403);
            }

            if (currentCard.getRequestBlocking() == RequestBlocking.YES) {
                return new MessageResponse("Карточка уже заблокирована!", 400);
            }

            // Блокируем карту
            int updatedRows = cardRepository.updateRequestBlocking("YES", cardId, userId);
            if (updatedRows != 1) {
                throw new RuntimeException("Не удалось отправить запрос на блокировку.");
            }

            return new MessageResponse("Карточка успешно заблокирована", 200);
        } else {
            return new MessageResponse("Пользователь не найден!", 404);
        }
    }
}
