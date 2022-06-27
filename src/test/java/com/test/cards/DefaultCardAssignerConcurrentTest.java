package com.test.cards;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import com.test.cards.domain.Album;
import com.test.cards.domain.AlbumSet;
import com.test.cards.domain.Card;
import com.test.cards.domain.Event;
import com.test.cards.service.CardAssigner;
import com.test.cards.service.ConfigurationProvider;
import com.test.cards.service.DefaultCardAssigner;
import org.junit.*;

import org.junit.Rule;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Sets.newHashSet;
import static com.test.cards.domain.Event.Type.ALBUM_FINISHED;
import static com.test.cards.domain.Event.Type.SET_FINISHED;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DefaultCardAssignerConcurrentTest {
    private static CardAssigner cardAssigner;
    private static CounterConsumer counter = new CounterConsumer();
    private static List<Long> users = generateUserIds(5);
    private final int SIX_CARDS = 6;
    private static ConfigurationProvider configurationProvider;

    @BeforeClass
    public static void setup() {
        configurationProvider = mock(ConfigurationProvider.class);
        when(configurationProvider.get()).thenReturn(
                new Album(0L, "Animals", newHashSet(
                        new AlbumSet(0L, "Birds", newHashSet(
                                new Card(0L, "Stork"),
                                new Card(1L, "Tit"),
                                new Card(2L, "Eagle")
                        )),
                        new AlbumSet(1L, "Fish", newHashSet(
                                new Card(3L, "Perch"),
                                new Card(4L, "Pike"),
                                new Card(5L, "Marlin")
                        ))
                ))
        );
        cardAssigner = new DefaultCardAssigner(configurationProvider);
        cardAssigner.subscribe(counter);
    }

    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();
    @Rule
    public RepeatingRule rule = new RepeatingRule();

    @Test(timeout = 60000L)
    @Concurrent(count = 1000)
    @Repeating(repetition = 10)
    public void assign_In_Concurrent_Environment() {
        int userId = ThreadLocalRandom.current().nextInt(0, users.size());
        int cardId = ThreadLocalRandom.current().nextInt(0, SIX_CARDS);
        cardAssigner.assignCard(userId, cardId);
    }

    @AfterClass
    public static void assert_Concurrent_Results() {
        assertEquals(users.size() * 2, counter.getSetFinished());
        assertEquals(users.size(), counter.getAlbumFinished());
    }

    @Test(timeout = 60000L)
    public void assign_In_Concurrent_Environment_Custom() {
        List<Event> newEvents = Collections.synchronizedList(new ArrayList<>());
        DefaultCardAssigner newCardAssigner = new DefaultCardAssigner(configurationProvider);
        newCardAssigner.subscribe(newEvents::add);
        final Album album = configurationProvider.get();

        final ExecutorService executorService = newFixedThreadPool(10);
        while (!isAlbumFinished(newEvents, album)) {
            executorService.submit(() -> {
                int cardId = ThreadLocalRandom.current().nextInt(0, SIX_CARDS);
                int userId = ThreadLocalRandom.current().nextInt(0, users.size());
                newCardAssigner.assignCard(userId, cardId);
            });
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        long countAlbums = newEvents.stream().filter(event -> event.type == ALBUM_FINISHED).count();
        long countSets = newEvents.stream().filter(event -> event.type == SET_FINISHED).count();
        assertEquals(users.size() * 2, countSets);
        assertEquals(users.size(), countAlbums);
    }

    private boolean isAlbumFinished(List<Event> events, Album album) {
        return events.size() == users.size() + users.size() * album.sets.size();
    }

    private static List<Long> generateUserIds(int max) {
        return range(0, max).boxed().collect(toList());
    }
}