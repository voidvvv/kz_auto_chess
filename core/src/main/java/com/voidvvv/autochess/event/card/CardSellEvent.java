package com.voidvvv.autochess.event.card;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.Card;

/**
 * Event sent when a card is sold
 */
public class CardSellEvent implements GameEvent {
    public final Card card;
    public final int goldReceived;
    private long timestamp;

    public CardSellEvent(Card card, int goldReceived) {
        this.card = card;
        this.goldReceived = goldReceived;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
