package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.Card;
import org.example.entity.User;
import org.example.exception.InsufficientFundsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CardTransferService {

    private final CardRepository cardRepository;


    private final UserRepository userRepository;

    public CardTransferService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Возвращает список карт пользователя.
     */
    public List<Card> getUserCards(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        User user = userOpt.get();
        return cardRepository.findAllByOwner(user);
    }

    @Transactional
    public void transferAmountBetweenCards(Long fromCardNumber, Long toCardNumber, BigDecimal amount) {

        Optional<Card> fromCardOpt = cardRepository.findByCardNumber(String.valueOf(fromCardNumber));
        Optional<Card> toCardOpt = cardRepository.findByCardNumber(String.valueOf(toCardNumber));

        if (fromCardOpt.isEmpty() || toCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Одна из указанных карт не найдена.");
        }
        Card fromCard = fromCardOpt.get();
        Card toCard = toCardOpt.get();

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
        List<Card> userCards = getUserCards(userId);
        System.out.println("userCards: " + userCards.toString());
        if (userCards.size() <= 1) {
            throw new IllegalStateException("Перевод возможен только при наличии двух или более карт.");
        }

        Optional<Card> sourceCardOpt = userCards.stream()
                .filter(card -> card.getCardNumber().equals(String.valueOf(sourceCard)))
                .findAny();

        Optional<Card> targetCardOpt = userCards.stream()
                .filter(card -> card.getCardNumber().equals(String.valueOf(targetCard)))
                .findAny();

        if (sourceCardOpt.isEmpty() || targetCardOpt.isEmpty()) {
            throw new IllegalArgumentException("Указанные карты не принадлежат указанному пользователю.");
        }

        if (sourceCard.equals(targetCard)) {
            throw new IllegalArgumentException("Источник и цель перевода не могут совпадать.");
        }

        transferAmountBetweenCards(sourceCard, targetCard, amount);
    }
}
