package org.example.controller;

import org.example.dto.CardBalanceDto;
import org.example.entity.User;
import org.example.exception.CustomBusinessException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/user/v1/cards")
public class UserCardController {

    private UserRepository userRepository;
    private UserCardService userCardService;

    public UserCardController(UserRepository userRepository, UserCardService userCardService) {
        this.userRepository = userRepository;
        this.userCardService = userCardService;
    }

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
    public ResponseEntity<Map<String, String>> depositCardBalance
            (@PathVariable Long cardId, @RequestParam BigDecimal amountChange) {
        BigDecimal newBalance = userCardService.depositCardBalance(cardId, amountChange);
        if (newBalance == null) {
            throw new CustomBusinessException("Проверьте введенные данные!");
        }
        Map<String, String> result = new HashMap<>();
        result.put("message", "Пополнение выполнено успешно!");
        result.put("balance", newBalance.toString());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/withdraw/{cardId}")
    @PreAuthorize(("isAuthenticated()"))
    public ResponseEntity<Map<String, String>> withdrawBalance(@PathVariable Long cardId, @RequestParam BigDecimal amountWithdraw)
            throws InsufficientFundsException {
        try {
            BigDecimal newBalance = userCardService.withdrawCardBalance(cardId, amountWithdraw);
            Map<String, String> result = new HashMap<>();
            result.put("message", "Списание выполнено успешно!");
            result.put("balance", newBalance.toString());
            return ResponseEntity.ok(result);
        }catch (NoSuchElementException e) {
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("message", "Card not found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResult);
        }catch (InsufficientFundsException e) {
            Map<String, String> resultError = new HashMap<>();
            resultError.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(resultError);
        }
    }
}
