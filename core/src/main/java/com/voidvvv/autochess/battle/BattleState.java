package com.voidvvv.autochess.battle;

import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.PhaseTransitionEvent;
import com.voidvvv.autochess.model.GamePhase;

/**
 * Manages mutable battle state separately from immutable context
 * Used by BattleManager to track state changes that need to be persisted
 */
public class BattleState {
    private BattleContext currentContext;
    private final GameEventSystem eventSystem;

    public BattleState(BattleContext initialContext, GameEventSystem eventSystem) {
        this.currentContext = initialContext;
        this.eventSystem = eventSystem;
    }

    /**
     * Transition to new phase and notify listeners
     */
    public void transitionTo(GamePhase newPhase) {
        GamePhase oldPhase = currentContext.getPhase();
        currentContext = currentContext.withPhase(newPhase);

        // Send phase transition event
        eventSystem.postEvent(new PhaseTransitionEvent(oldPhase, newPhase));
    }

    /**
     * Get current immutable context snapshot
     */
    public BattleContext getContext() {
        return currentContext;
    }

    /**
     * Update context (e.g., after round complete)
     */
    public void updateContext(BattleContext newContext) {
        this.currentContext = newContext;
    }

    /**
     * Update round number
     */
    public void nextRound() {
        currentContext = currentContext.withRoundNumber(currentContext.getRoundNumber() + 1);
    }
}
