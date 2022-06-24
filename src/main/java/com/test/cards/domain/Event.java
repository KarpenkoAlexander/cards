package com.test.cards.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class Event {

    public long userId;
    public Type type;

    public enum Type {
        SET_FINISHED, ALBUM_FINISHED
    }
}
