package com.voidvvv.autochess.event;

import com.voidvvv.autochess.model.GamePhase;

/**
 * Event sent when battle ends (BATTLE → PLACEMENT transition)
 * Contains battle result (win/lose) for economy calculations
 * Also contains remaining enemies for life system damage calculation
 */
public class BattleEndEvent implements GameEvent {
    public final GamePhase fromPhase;
    public final GamePhase toPhase;
    public final boolean playerWon;
    public final int remainingEnemies;
    private long timestamp;

    public BattleEndEvent(boolean playerWon, int remainingEnemies) {
        this.fromPhase = GamePhase.BATTLE;
        this.toPhase = GamePhase.PLACEMENT;
        this.playerWon = playerWon;
        this.remainingEnemies = remainingEnemies;
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
