package com.test.cards.domain;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Set;

@AllArgsConstructor
@ToString
public class Album {

    public long id;
    public String name;
    public Set<AlbumSet> sets;

}
