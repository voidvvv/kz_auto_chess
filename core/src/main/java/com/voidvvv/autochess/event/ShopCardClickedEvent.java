package com.voidvvv.autochess.event;

import com.voidvvv.autochess.model.Card;

/**
 * UI点击事件
 */
public class ShopCardClickedEvent implements GameEvent {
    private final Card card;
    private long timestamp;

    public ShopCardClickedEvent(Card card) {
        this.card = card;
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

    public Card getCard() {
        return card;
    }
}
