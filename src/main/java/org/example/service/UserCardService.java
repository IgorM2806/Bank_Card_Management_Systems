package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dto.CardBalanceDto;
import org.example.entity.Card;
import org.example.entity.User;
import org.example.exception.InsufficientFundsException;
import org.example.repository.CardRepository;
import org.example.util.MaskCardNumber;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserCardService {

    private final CardRepository cardRepository;

    public UserCardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<CardBalanceDto> viewBalancesForUser (User currentUser) {
        List<Card> cards = cardRepository.findAllByOwner(currentUser);

        MaskCardNumber masker = new MaskCardNumber();

        return cards.stream()
                .map(card -> new CardBalanceDto(masker.maskCardNumber(card.getCardNumber()),
                        card.getBalance())).collect(Collectors.toList());
    }

    @Transactional
    public BigDecimal depositCardBalance(Long cardId, BigDecimal amountDeposit) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Карточка с указанным ID не найдена."));

        BigDecimal updatedBalance = card.getBalance().add(amountDeposit);
        card.setBalance(updatedBalance);
        cardRepository.save(card);
        return updatedBalance;
    }

    @Transactional
    public BigDecimal withdrawCardBalance(Long cardId, BigDecimal amountWithdraw) throws InsufficientFundsException {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Карточка с указанным ID не найдена."));

        if (amountWithdraw.compareTo(BigDecimal.ZERO) > 0 &&
                amountWithdraw.compareTo(card.getBalance()) > 0) {
            throw new InsufficientFundsException("Недостаточно средств на счету карты №" + card.getCardNumber() +
                    ". Доступный остаток: " + card.getBalance());
        }

        BigDecimal updatedBalance = card.getBalance().subtract(amountWithdraw);
        card.setBalance(updatedBalance);
        cardRepository.save(card);
        return updatedBalance;
    }
}
