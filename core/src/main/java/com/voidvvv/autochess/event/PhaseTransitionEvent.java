package com.voidvvv.autochess.event;

import com.voidvvv.autochess.model.GamePhase;

/**
 * Event for phase transitions
 * Sent when game phase changes (e.g., PLACEMENT → BATTLE)
 */
public class PhaseTransitionEvent implements GameEvent {
    public final GamePhase fromPhase;
    public final GamePhase toPhase;
    private long timestamp;

    public PhaseTransitionEvent(GamePhase fromPhase, GamePhase toPhase) {
        this.fromPhase = fromPhase;
        this.toPhase = toPhase;
        this.timestamp = System.currentTimeMillis();
    }

    public GamePhase getFromPhase() {
        return fromPhase;
    }

    public GamePhase getToPhase() {
        return toPhase;
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
