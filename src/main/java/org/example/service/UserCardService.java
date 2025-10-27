package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.CardBalanceDto;
import org.example.entity.Cards;
import org.example.entity.Users;
import org.example.exception.InsufficientFundsException;
import org.example.repository.CardRepository;
import org.example.util.MaskCardNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserCardService {

    private final CardRepository cardRepository;

    public List<CardBalanceDto> viewBalancesForUser (Users currentUser) {
        List<Cards> cards = cardRepository.findAllByOwner(currentUser);

        MaskCardNumber masker = new MaskCardNumber();

        return cards.stream()
                .map(card -> new CardBalanceDto(masker.maskCardNumber(card.getCardNumber()),
                        card.getBalance())).collect(Collectors.toList());
    }

    @Transactional
    public void depositCardBalance(Long cardId, BigDecimal amountDeposit) {
        Cards card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Карточка с указанным ID не найдена."));

        BigDecimal updatedBalance = card.getBalance().add(amountDeposit);
        card.setBalance(updatedBalance);
        cardRepository.save(card);
    }

    @Transactional
    public void withdrawCardBalance(Long cardId, BigDecimal amountWithdraw) throws InsufficientFundsException {
        Cards card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Карточка с указанным ID не найдена."));

        if (amountWithdraw.compareTo(BigDecimal.ZERO) > 0 &&
                amountWithdraw.compareTo(card.getBalance()) > 0) {
            throw new InsufficientFundsException("Недостаточно средств на счету карты №" + card.getCardNumber() +
                    ". Доступный остаток: " + card.getBalance());
        }

        BigDecimal updatedBalance = card.getBalance().subtract(amountWithdraw);
        card.setBalance(updatedBalance);
        cardRepository.save(card);
    }
}
