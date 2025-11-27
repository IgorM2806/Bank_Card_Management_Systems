package org.example.service;

import org.example.entity.Card;
import org.example.entity.Status;
import org.springframework.stereotype.Component;

@Component
public class BlockedCardStatusChangeHandler implements CardStatusChangeHandler{
    @Override
    public boolean canHandle(Status status){
        return status.equals(Status.BLOCKED);
    }

    @Override
    public void handle(Card card, Status newStatus) {
        card.setSuspicious(true);
        card.setStatus(newStatus);
    }


}
