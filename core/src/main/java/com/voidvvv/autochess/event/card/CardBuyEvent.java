package com.voidvvv.autochess.event.card;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.Card;

/**
 * Event sent when a card is bought from the shop
 */
public class CardBuyEvent implements GameEvent {
    public final Card card;
    public final int cost;
    private long timestamp;

    public CardBuyEvent(Card card, int cost) {
        this.card = card;
        this.cost = cost;
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
