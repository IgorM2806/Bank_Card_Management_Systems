package org.example.controller;

import org.example.exception.CardNotFoundException;
import org.example.service.CardDeletionAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/cards")
public class AdminDeleteCardController {

    private final CardDeletionAdminService cardDeletionAdminService;

    public AdminDeleteCardController(CardDeletionAdminService cardDeletionAdminService) {
        this.cardDeletionAdminService = cardDeletionAdminService;
    }

    @DeleteMapping("/delete/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long cardId){
        System.out.println("Контроллер - AdminDeleteCardController!");
            cardDeletionAdminService.deleteCard(cardId);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(CardNotFoundException.class)
    public Map<String, Object> handleCardNotFoundException(CardNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return response;
    }

}
