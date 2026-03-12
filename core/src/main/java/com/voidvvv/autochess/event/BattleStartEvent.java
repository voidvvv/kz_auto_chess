package com.voidvvv.autochess.event;

import com.voidvvv.autochess.model.GamePhase;

/**
 * Event sent when battle starts (PLACEMENT → BATTLE transition)
 * Listeners can respond to this event by updating UI, disabling shop, etc.
 */
public class BattleStartEvent implements GameEvent {
    public final GamePhase fromPhase;
    public final GamePhase toPhase;
    private long timestamp;

    public BattleStartEvent() {
        this.fromPhase = GamePhase.PLACEMENT;
        this.toPhase = GamePhase.BATTLE;
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
