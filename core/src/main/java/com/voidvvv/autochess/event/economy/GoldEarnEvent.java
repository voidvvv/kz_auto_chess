package com.voidvvv.autochess.event.economy;

import com.voidvvv.autochess.event.GameEvent;

/**
 * Event sent when gold is earned
 * Used by EconomyManager to notify listeners of gold income
 */
public class GoldEarnEvent implements GameEvent {
    public final int amount;
    public final String source; // e.g., "battle_reward", "interest", etc.
    private long timestamp;

    public GoldEarnEvent(int amount, String source) {
        this.amount = amount;
        this.source = source;
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
