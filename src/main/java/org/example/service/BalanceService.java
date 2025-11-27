package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.entity.Card;
import org.example.exception.InsufficientFundsException;
import org.example.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceService {
    private final CardRepository cardRepository;

    @Autowired
    public BalanceService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public BigDecimal deposit(Long cardId, BigDecimal amountDeposit) {
        Card card = findCardOrFail(cardId);
        BigDecimal updateBalance = card.getBalance().add(amountDeposit);
        card.setBalance(updateBalance);
        cardRepository.save(card);
        return updateBalance;
    }

    public BigDecimal withdraw(Long cardId, BigDecimal amountWithdraw)  throws InsufficientFundsException {
        Card card = findCardOrFail(cardId);
        if (amountWithdraw.compareTo(BigDecimal.ZERO) <= 0 || amountWithdraw.compareTo(card.getBalance()) > 0) {
            throw new InsufficientFundsException("Недостаточно средств на счете.");
        }

        BigDecimal updatedBalance = card.getBalance().subtract(amountWithdraw);
        card.setBalance(updatedBalance);
        cardRepository.save(card);
        return updatedBalance;
    }

    private Card findCardOrFail(Long cardId) {
        try {
            return cardRepository.findById(cardId).orElseThrow(() -> new EmptyResultDataAccessException(1));
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Карточка с данным ID не существует");
        }
    }
}
