package com.test.cards.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.Set;

@AllArgsConstructor
@EqualsAndHashCode
public class AlbumSet {
    public long id;
    public String name;
    public Set<Card> cards;
}
