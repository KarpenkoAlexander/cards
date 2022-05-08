package com.test.cards.domain;

import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class Album {

    public long id;
    public String name;
    public Set<AlbumSet> sets;

}
