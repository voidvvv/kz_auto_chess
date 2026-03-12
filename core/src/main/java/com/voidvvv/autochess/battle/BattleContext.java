package com.voidvvv.autochess.battle;

import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.model.PlayerEconomy;

/**
 * Battle context - aggregates all battle-related configuration for shared data access
 * Fully immutable context with mutable state managed separately by BattleState
 *
 * bbList (BattleUnitBlackboard list) is NOT stored here because it is inherently mutable
 * (characters are added/removed during battle). It is managed by BattleManager.
 */
public final class BattleContext {
    private final Battlefield battlefield;
    private final GamePhase phase;
    private final PlayerEconomy playerEconomy;
    private final SynergyManager synergyManager;
    private final int roundNumber;

    private BattleContext(Builder builder) {
        this.battlefield = builder.battlefield;
        this.phase = builder.phase;
        this.playerEconomy = builder.playerEconomy;
        this.synergyManager = builder.synergyManager;
        this.roundNumber = builder.roundNumber;
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public PlayerEconomy getPlayerEconomy() {
        return playerEconomy;
    }

    public SynergyManager getSynergyManager() {
        return synergyManager;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public BattleContext withPhase(GamePhase newPhase) {
        return new Builder()
                .setBattlefield(this.battlefield)
                .setPhase(newPhase)
                .setPlayerEconomy(this.playerEconomy)
                .setSynergyManager(this.synergyManager)
                .setRoundNumber(this.roundNumber)
                .build();
    }

    public BattleContext withRoundNumber(int newRoundNumber) {
        return new Builder()
                .setBattlefield(this.battlefield)
                .setPhase(this.phase)
                .setPlayerEconomy(this.playerEconomy)
                .setSynergyManager(this.synergyManager)
                .setRoundNumber(newRoundNumber)
                .build();
    }

    public static class Builder {
        private Battlefield battlefield;
        private GamePhase phase;
        private PlayerEconomy playerEconomy;
        private SynergyManager synergyManager;
        private int roundNumber = 1;

        public Builder setBattlefield(Battlefield battlefield) {
            this.battlefield = battlefield;
            return this;
        }

        public Builder setPhase(GamePhase phase) {
            this.phase = phase;
            return this;
        }

        public Builder setPlayerEconomy(PlayerEconomy playerEconomy) {
            this.playerEconomy = playerEconomy;
            return this;
        }

        public Builder setSynergyManager(SynergyManager synergyManager) {
            this.synergyManager = synergyManager;
            return this;
        }

        public Builder setRoundNumber(int roundNumber) {
            this.roundNumber = roundNumber;
            return this;
        }

        public BattleContext build() {
            if (battlefield == null) {
                throw new IllegalStateException("Battlefield is required");
            }
            if (phase == null) {
                throw new IllegalStateException("GamePhase is required");
            }
            return new BattleContext(this);
        }
    }
}
