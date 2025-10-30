package org.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardBlockResponseDto {
    private Long userId;
    private Long cardId;

    public  CardBlockResponseDto(Long userId, Long cardId) {
        this.userId = userId;
        this.cardId = cardId;
    }
}
