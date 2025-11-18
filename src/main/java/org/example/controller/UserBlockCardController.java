package org.example.controller;

import org.example.exception.UserNotFoundException;
import org.example.service.MessageResponse;
import org.example.service.UserRequestCardBlockService;
import org.example.service.UserUnblockCardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/v1/cards")
public class UserBlockCardController {

    private final UserRequestCardBlockService cardBlockService;

    private final UserUnblockCardService unblockCardService;

    public UserBlockCardController(UserRequestCardBlockService cardBlockService,  UserUnblockCardService unblockCardService) {
        this.cardBlockService = cardBlockService;
        this.unblockCardService = unblockCardService;
    }


    @RequestMapping("/block")
    @PostMapping
    public ResponseEntity<MessageResponse> blockCard(@RequestBody Map<String, Object> requestBody)
            throws AccessDeniedException {
        Long userId = ((Number) requestBody.get("userId")).longValue();
        Long cardId = ((Number) requestBody.get("cardId")).longValue();

        try {
            MessageResponse response = cardBlockService.requestBlockCard(userId, cardId);
            return ResponseEntity.status(response.getCode()).body(response);
        }catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(
                    "Пользователь не найден", 404));
        }
    }

    @PostMapping("/unblock")
    public ResponseEntity<MessageResponse> unBlockCard(@RequestBody Map<String, Object> requestBody) {
        Long userId = ((Number) requestBody.get("userId")).longValue();
        Long cardId = ((Number) requestBody.get("cardId")).longValue();
        try {
            MessageResponse response = unblockCardService.requestUnBlockCard(userId, cardId);
            return ResponseEntity.status(response.getCode()).body(response);
        } catch (AccessDeniedException | UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(ex.getMessage(), 403));
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError().body
                    (new MessageResponse("Не удалось обработать запрос: " + ex.getMessage(), 500));
        }
    }
}