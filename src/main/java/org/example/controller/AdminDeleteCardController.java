package org.example.controller;

import org.example.exception.CardNotFoundException;
import org.example.service.CardDeletionAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cards")
public class AdminDeleteCardController {

    private final CardDeletionAdminService cardDeletionAdminService;

    public AdminDeleteCardController(CardDeletionAdminService cardDeletionAdminService) {
        this.cardDeletionAdminService = cardDeletionAdminService;
    }

    /**
     * Endpoint для удаления карты по её идентификатору.
     *
     * @param cardId уникальный идентификатор карты.
     * @return успешный ответ с кодом 204 No Content, если карта успешно удалена.
     * @throws CardNotFoundException если карта с указанным идентификатором не найдена.
     */

    @DeleteMapping("/delete/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long cardId){
        System.out.println("Контроллер - AdminDeleteCardController!");
            cardDeletionAdminService.deleteCard(cardId);
    }

    // Обработчик исключений
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(CardNotFoundException.class)
    public Map<String, Object> handleCardNotFoundException(CardNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return response;
    }

}
