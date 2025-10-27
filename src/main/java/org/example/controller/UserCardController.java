package org.example.controller;

import org.example.dto.CardBalanceDto;
import org.example.entity.User;
import org.example.exception.InsufficientFundsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.example.service.UserCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/cards")
public class UserCardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCardService userCardService;

    @GetMapping("/balances/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CardBalanceDto>> getCardsWithBalancesForUser(@PathVariable Long userId) {
        System.out.println("Контроллер - getCardsWithBalancesForUser");
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
        List<CardBalanceDto> result = userCardService.viewBalancesForUser(currentUser);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/change-deposit/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> depositCardBalance (@PathVariable Long cardId, @RequestParam BigDecimal amountChange) {
        System.out.println("Контроллер: changeCardBalance!");

        userCardService.depositCardBalance(cardId, amountChange);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/withdraw/{cardId}")
    @PreAuthorize(("isAuthenticated()"))
    public ResponseEntity<Object> withdrawBalance(@PathVariable Long cardId, @RequestParam BigDecimal amountWithdraw)
            throws InsufficientFundsException {
        try {
            userCardService.withdrawCardBalance(cardId, amountWithdraw);
            return ResponseEntity.noContent().build();
        }catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());
        }
    }

}
