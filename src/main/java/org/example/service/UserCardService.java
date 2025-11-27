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
    private final BalanceService balanceService;

    public UserCardService(CardRepository cardRepository,  BalanceService balanceService) {
        this.cardRepository = cardRepository;
        this.balanceService = balanceService;
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
        return balanceService.deposit(cardId, amountDeposit);
    }

    @Transactional
    public BigDecimal withdrawCardBalance(Long cardId, BigDecimal amountWithdraw) throws InsufficientFundsException {
        return balanceService.withdraw(cardId, amountWithdraw);
    }
}
