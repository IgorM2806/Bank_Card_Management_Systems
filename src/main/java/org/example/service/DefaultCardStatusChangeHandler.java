package org.example.service;

import org.example.entity.Card;
import org.example.entity.Status;
import org.springframework.stereotype.Component;

@Component
public class DefaultCardStatusChangeHandler implements CardStatusChangeHandler {
    @Override
    public boolean canHandle(Status status) {
        return true;
    }

    @Override
    public void handle(Card card, Status newStatus) {
        card.setStatus(newStatus);
    }
}

