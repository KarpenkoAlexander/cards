package com.test.cards;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import com.test.cards.domain.Album;
import com.test.cards.domain.AlbumSet;
import com.test.cards.domain.Card;
import com.test.cards.service.CardAssigner;
import com.test.cards.service.ConfigurationProvider;
import com.test.cards.service.DefaultCardAssigner;
import org.junit.*;

import org.junit.Rule;
//import org.junit.jupiter.api.Test;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DefaultCardAssignerConcurrentTest {
    private static CardAssigner cardAssigner;
    private static CounterConsumer counter = new CounterConsumer();

    private static List<Long> userCards = generateUserIds(10);

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


    @Test
    @Concurrent(count = 1000)
    @Repeating(repetition = 1)
    public void runsMultipleTimesRandom() {
        int userId = ThreadLocalRandom.current().nextInt(0, 2);
        int cardId = ThreadLocalRandom.current().nextInt(0, 6);
        cardAssigner.assignCard(userId, cardId);

    }

    @AfterClass
    public static void annotatedTestRunsMultipleTimes() throws InterruptedException {
        assertEquals(4, counter.getSetFinished());
        assertEquals(2, counter.getAlbumFinished());
    }

    private static List<Long> generateUserIds(long size) {
        return range(0L, size)
                .boxed()
                .collect(toList());
    }


}