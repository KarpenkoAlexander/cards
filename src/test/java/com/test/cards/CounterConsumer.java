package com.test.cards;

import com.test.cards.domain.Event;

import java.util.function.Consumer;

public class CounterConsumer implements Consumer<Event> {
    private int setFinished = 0;
    private int albumFinished = 0;

    @Override
    public void accept(Event event) {
        if (Event.Type.SET_FINISHED.equals(event.type)) {
            setFinished++;
        } else {
            albumFinished++;
        }
    }

    public int getSetFinished() {
        return setFinished;
    }

    public int getAlbumFinished() {
        return albumFinished;
    }

}
