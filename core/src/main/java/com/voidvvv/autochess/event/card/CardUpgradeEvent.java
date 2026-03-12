package com.voidvvv.autochess.event.card;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.Card;

/**
 * Event sent when a card is upgraded
 */
public class CardUpgradeEvent implements GameEvent {
    public final Card originalCard;
    public final Card upgradedCard;
    public final int cost;
    private long timestamp;

    public CardUpgradeEvent(Card originalCard, Card upgradedCard, int cost) {
        this.originalCard = originalCard;
        this.upgradedCard = upgradedCard;
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
