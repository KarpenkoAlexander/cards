package com.test.cards.service;

import com.test.cards.domain.Album;
import com.test.cards.domain.AlbumSet;
import com.test.cards.domain.Card;
import com.test.cards.domain.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DefaultCardAssigner implements CardAssigner {

    private final int FIXED_CARD_NUM_IN_SET = 3;
    private final int FIXED_CARD_NUM_IN_ALBUM = 6;
    private final List<Consumer<Event>> subscribers = new ArrayList<>();
    private final Album fullAlbum;
    private final Map<Long, AlbumSet> albumSetMap = new HashMap<>();
    private final Map<Long, Card> cardsMap = new HashMap<>();
    private final Map<Long, Album> userAlbumMap = new HashMap<>();

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
    public void assignCard(long userId, long cardId) {
        Album userAlbum;
        synchronized (userAlbumMap) {
            Album album = userAlbumMap.get(userId);
            userAlbum = Optional.ofNullable(album).orElseGet(() -> createUserAlbum(userId));
        }
        synchronized (userAlbum.sets) {
            boolean isAlbumSetFull = addCardToAlbumSet(cardId, userAlbum);

            if (isAlbumSetFull) {
                triggerEvent(new Event(userId, Event.Type.SET_FINISHED));
            }

            if (isAlbumSetFull && isAlbumFull(userAlbum)) {
                triggerEvent(new Event(userId, Event.Type.ALBUM_FINISHED));
            }
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
        return userAlbumSet.cards.add(card) && userAlbumSet.cards.size() == FIXED_CARD_NUM_IN_SET;
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

    private void triggerEvent(Event event) {
        for (Consumer<Event> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }
}
