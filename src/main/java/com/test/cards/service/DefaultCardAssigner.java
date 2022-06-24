package com.test.cards.service;

import com.test.cards.domain.Album;
import com.test.cards.domain.AlbumSet;
import com.test.cards.domain.Card;
import com.test.cards.domain.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DefaultCardAssigner implements CardAssigner {

    private final Map<Long, Album> userAlbumMap = new ConcurrentHashMap<>();
    private final Map<Long, AlbumSet> albumSetMap = new ConcurrentHashMap<>();
    private final Map<Long, Card> cardsMap = new ConcurrentHashMap<>();
    private final int FIXED_CARD_NUM_IN_SET = 3;
    private final int FIXED_CARD_NUM_IN_ALBUM = 6;
    private final List<Consumer<Event>> subscribers = new ArrayList<>();
    private final Album fullAlbum;
    private final List<Event> events = new ArrayList<>();
    private final Object monitor = new Object();


    public DefaultCardAssigner(ConfigurationProvider configurationProvider) {
        this.fullAlbum = configurationProvider.get();
        fullAlbum.sets.forEach(albumSet -> {
            albumSet.cards.forEach(card -> {
                cardsMap.put(card.id, card);
                albumSetMap.put(card.id, albumSet);
            });
        });
    }

    @Override
    synchronized public void assignCard(long userId, long cardId) {
        Album album = userAlbumMap.get(userId);
        Album userAlbum = Optional.ofNullable(album).orElseGet(() -> createUserAlbum(userId));
        boolean isAlbumSetFull = addCardToAlbumSet(cardId, userAlbum);

        if (isAlbumSetFull) {
            triggerEvent(new Event(userId, Event.Type.SET_FINISHED));
        }

        if (isAlbumSetFull && isAlbumFull(userAlbum)) {
            triggerEvent(new Event(userId, Event.Type.ALBUM_FINISHED));
        }
    }

    private boolean isAlbumFull(Album userAlbum) {
        int totalSet = userAlbum.sets.stream()
                .mapToInt(set -> set.cards.size())
                .sum();
        return totalSet == FIXED_CARD_NUM_IN_ALBUM;

    }

    private boolean addCardToAlbumSet(long cardId, Album userAlbum) {
        AlbumSet albumSet = albumSetMap.get(cardId);
        Card card = cardsMap.get(cardId);

        AlbumSet userAlbumSet = userAlbum.sets.stream()
                .filter(albumSetUser -> albumSetUser.id == albumSet.id)
                .findFirst()
                .get();
        //System.out.println(userAlbumSet.cards.getClass());
       // synchronized (userAlbumSet.cards) {
            if (userAlbumSet.cards.size() == FIXED_CARD_NUM_IN_SET) {
                return false;
            }
            return userAlbumSet.cards.add(card) && userAlbumSet.cards.size() == FIXED_CARD_NUM_IN_SET;
    //    }
    }


    private Album createUserAlbum(Long userId) {
        final Album album = new Album(fullAlbum.id, fullAlbum.name, Collections.synchronizedSet(new HashSet<>()));
        fullAlbum.sets.forEach(albumSet -> {
            album.sets.add(new AlbumSet(albumSet.id, albumSet.name, Collections.synchronizedSet(new HashSet<>())));
        });
        userAlbumMap.put(userId, album);
        return album;
    }

    @Override
    public void subscribe(Consumer<Event> consumer) {
        subscribers.add(consumer);
    }

    private List<Event> triggerEvent(Event event) {
        for (Consumer<Event> subscriber : subscribers) {
            subscriber.accept(event);
        }
        events.add(event);
        return events;
    }
}
