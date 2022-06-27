package com.test.cards;

import com.test.cards.domain.Album;
import com.test.cards.domain.AlbumSet;
import com.test.cards.domain.Card;
import com.test.cards.domain.Event;
import com.test.cards.service.CardAssigner;
import com.test.cards.service.ConfigurationProvider;
import com.test.cards.service.DefaultCardAssigner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.test.cards.domain.Event.Type.ALBUM_FINISHED;
import static com.test.cards.domain.Event.Type.SET_FINISHED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class DefaultCardAssignerTest {

    @Mock
    private ConfigurationProvider configurationProvider;

    private CardAssigner cardAssigner;

    @Before
    public void configure() {
        when(configurationProvider.get()).thenReturn(
                new Album(1L, "Animals", newHashSet(
                        new AlbumSet(1L, "Birds", newHashSet(
                                new Card(1L, "Stork"),
                                new Card(2L, "Tit"),
                                new Card(3L, "Eagle")
                        )),
                        new AlbumSet(2L, "Fish", newHashSet(
                                new Card(4L, "Perch"),
                                new Card(5L, "Pike"),
                                new Card(6L, "Marlin")
                        ))
                ))
        );
        cardAssigner = new DefaultCardAssigner(configurationProvider);
    }

    @Test
    public void check_NotNull_Album() {
        Assert.assertNotNull(configurationProvider.get());
    }

    @Test
    public void assign_3Cards_For_2Users_And_Expect_SetFinished_EventFired() {
        final List<Event> events = new ArrayList<>();
        cardAssigner.subscribe(events::add);
        Iterator<AlbumSet> setIterator = configurationProvider.get().sets.iterator();

        final int userId = 1;
        setIterator.next().cards.forEach(card -> cardAssigner.assignCard(userId, card.id));
        cardAssigner.assignCard(userId, 3); // duplicate card won't populate event
        assertEquals(1, events.size());
        Event event = events.get(0);
        assertEquals(userId, event.userId);
        assertEquals(SET_FINISHED, event.type);

        final int userId_ = 2;
        setIterator.next().cards.forEach(card -> cardAssigner.assignCard(userId_, card.id));
        assertEquals(2, events.size());
        event = events.get(1);
        assertEquals(userId_, event.userId);
        assertEquals(SET_FINISHED, event.type);
    }

    @Test
    public void assign_6_Cards_And_Expect_AlbumFinishedEventFired() {
        final List<Event> events = new ArrayList<>();
        cardAssigner.subscribe(events::add);

        final int userId = 1;
        configurationProvider.get().sets.stream().flatMap(set -> set.cards.stream())
                .forEach(card -> cardAssigner.assignCard(userId, card.id));
        cardAssigner.assignCard(userId, 6); // duplicate card won't populate event
        assertEquals(3, events.size());
        final Event event = events.get(2);
        assertEquals(userId, event.userId);
        assertEquals(ALBUM_FINISHED, event.type);
    }

}