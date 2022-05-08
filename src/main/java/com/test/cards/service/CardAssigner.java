package com.test.cards.service;

import com.test.cards.domain.Event;

import java.util.function.Consumer;

public interface CardAssigner {

    void assignCard(long userId, long cardId);

    void subscribe(Consumer<Event> consumer);
}
