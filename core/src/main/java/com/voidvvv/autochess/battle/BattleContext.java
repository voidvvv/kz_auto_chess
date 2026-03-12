package com.voidvvv.autochess.battle;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.model.PlayerEconomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Battle context - aggregates all battle-related objects for shared data access
 * Immutable context with mutable state managed separately
 *
 * DESIGN DECISION: Use immutable context snapshot + state manager pattern
 * - BattleContext is fully immutable (created via Builder)
 * - Phase mutations happen through BattleState manager
 * - This prevents inconsistent state and makes testing easier
 */
public final class BattleContext {
    private final Battlefield battlefield;
    private final List<BattleUnitBlackboard> bbList;
    private final GamePhase phase;  // Immutable phase at construction time
    private final PlayerEconomy playerEconomy;  // Reference, not owned
    private final SynergyManager synergyManager;  // Reference
    private final int roundNumber;

    private BattleContext(Builder builder) {
        this.battlefield = builder.battlefield;
        this.bbList = List.copyOf(builder.bbList);
        this.phase = builder.phase;
        this.playerEconomy = builder.playerEconomy;
        this.synergyManager = builder.synergyManager;
        this.roundNumber = builder.roundNumber;
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public List<BattleUnitBlackboard> getBbList() {
        return bbList;
    }

    public GamePhase getPhase() {
        return phase;  // Returns immutable phase snapshot
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

    /**
     * Create a new context with updated phase (immutable pattern)
     * @return New BattleContext with updated phase
     */
    public BattleContext withPhase(GamePhase newPhase) {
        return new Builder()
                .setBattlefield(this.battlefield)
                .setBbList(new ArrayList<>(this.bbList))
                .setPhase(newPhase)
                .setPlayerEconomy(this.playerEconomy)
                .setSynergyManager(this.synergyManager)
                .setRoundNumber(this.roundNumber)
                .build();
    }

    /**
     * Create a new context with updated round number (immutable pattern)
     * @return New BattleContext with updated round
     */
    public BattleContext withRoundNumber(int newRoundNumber) {
        return new Builder()
                .setBattlefield(this.battlefield)
                .setBbList(new ArrayList<>(this.bbList))
                .setPhase(this.phase)
                .setPlayerEconomy(this.playerEconomy)
                .setSynergyManager(this.synergyManager)
                .setRoundNumber(newRoundNumber)
                .build();
    }

    /**
     * Builder for safe construction
     */
    public static class Builder {
        private Battlefield battlefield;
        private List<BattleUnitBlackboard> bbList = new ArrayList<>();
        private GamePhase phase;
        private PlayerEconomy playerEconomy;
        private SynergyManager synergyManager;
        private int roundNumber = 1;

        public Builder setBattlefield(Battlefield battlefield) {
            this.battlefield = battlefield;
            return this;
        }

        public Builder setBbList(List<BattleUnitBlackboard> bbList) {
            this.bbList = new ArrayList<>(bbList);
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
            // Validate required fields
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
