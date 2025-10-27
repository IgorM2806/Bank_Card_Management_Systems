package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.entity.Cards;
import org.example.entity.User;
import org.example.exception.InsufficientFundsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CardTransferService {

    private final CardRepository cardRepository;


    private final UserRepository userRepository;

    /**
     * Возвращает список карт пользователя.
     */
    public List<Cards> getUserCards(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        User user = userOpt.get();
        return cardRepository.findAllByOwner(user);
    }

    @Transactional
    public void transferAmountBetweenCards(Long fromCardId, Long toCardId, BigDecimal amount) {

        Optional<Cards> fromCardOpt = cardRepository.findById(fromCardId);
        Optional<Cards> toCardOpt = cardRepository.findById(toCardId);
        if (fromCardOpt.isEmpty() || toCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Одна из указанных карт не найдена.");
        }
        Cards fromCard = fromCardOpt.get();
        Cards toCard = toCardOpt.get();

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на исходной карте.");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Transactional
    public void performTransferForUser(Long userId, Long sourceCard, Long targetCard, BigDecimal amount) {
        List<Cards> userCards = getUserCards(userId);

        if (userCards.size() <= 1){
            throw new IllegalStateException("Перевод возможен только при наличии двух или более карт.");
        }

        boolean isSourceCardValid = false;
        boolean isTargetCardValid = false;

        for (Cards card : userCards) {
            if(card.getId() == sourceCard){
                isSourceCardValid = true;
            }
            if(card.getId() == targetCard){
                isTargetCardValid = true;
            }
        }
        if (!isSourceCardValid || !isTargetCardValid){
            throw new IllegalArgumentException("Указанные карты не принадлежат указанному пользователю.");
        }

        transferAmountBetweenCards(sourceCard, targetCard, amount);
    }
}
