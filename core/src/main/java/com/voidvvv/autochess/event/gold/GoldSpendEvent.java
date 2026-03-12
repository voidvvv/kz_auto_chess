package com.voidvvv.autochess.event.gold;

import com.voidvvv.autochess.event.GameEvent;

/**
 * Event sent when gold is spent
 * Used by EconomyManager to notify listeners of gold expenditure
 */
public class GoldSpendEvent implements GameEvent {
    public final int amount;
    public final String reason; // e.g., "buy_card", "refresh_shop", "upgrade_card", etc.
    private long timestamp;

    public GoldSpendEvent(int amount, String reason) {
        this.amount = amount;
        this.reason = reason;
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
