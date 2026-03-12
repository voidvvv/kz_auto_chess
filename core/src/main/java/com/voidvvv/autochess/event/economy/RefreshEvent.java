package com.voidvvv.autochess.event.economy;

import com.voidvvv.autochess.event.GameEvent;

/**
 * Event sent when shop is refreshed
 * Used to notify listeners that shop cards have been refreshed
 */
public class RefreshEvent implements GameEvent {
    public final int cost;
    private long timestamp;

    public RefreshEvent(int cost) {
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
