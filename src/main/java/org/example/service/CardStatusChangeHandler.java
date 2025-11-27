package org.example.service;

import org.example.entity.Card;
import org.example.entity.Status;

public interface CardStatusChangeHandler {
    boolean canHandle(Status status);

    void handle(Card card, Status newStatus);
}
