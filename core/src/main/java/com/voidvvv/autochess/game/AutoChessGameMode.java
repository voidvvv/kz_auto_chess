package com.voidvvv.autochess.game;

import com.voidvvv.autochess.battle.BattleContext;
import com.voidvvv.autochess.battle.BattleState;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.input.GameInputHandler;
import com.voidvvv.autochess.input.InputContext;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.render.RenderCoordinator;

/**
 * AutoChess game mode implementation
 * Coordinates all battle-related managers via event system (no direct dependencies)
 *
 * DESIGN DECISION: Fully decoupled managers
 * - Managers communicate ONLY through GameEventSystem
 * - No direct method calls between managers
 * - Input handling delegated to GameInputHandler, not managers directly
 */
public class AutoChessGameMode implements GameMode {

    // Placeholder managers (will be implemented in later phases)
    // private final BattleManager battleManager;
    // private final EconomyManager economyManager;
    // private final CardManager cardManager;

    private final BattleState battleState;
    private final RenderCoordinator renderCoordinator;
    private final GameEventSystem eventSystem;
    private final GameInputHandler inputHandler;

    public AutoChessGameMode(BattleContext battleContext,
                              RenderCoordinator renderCoordinator,
                              GameEventSystem eventSystem,
                              GameInputHandler inputHandler) {
        this.battleState = new BattleState(battleContext, eventSystem);
        this.renderCoordinator = renderCoordinator;
        this.eventSystem = eventSystem;
        this.inputHandler = inputHandler;
    }

    @Override
    public void onEnter() {
        // Initialize managers (placeholder for Phase 1)
        // battleManager.onEnter();
        // economyManager.onEnter();
        // cardManager.onEnter();

        // Initialize input handler
        inputHandler.initialize(this);
    }

    @Override
    public void update(float delta) {
        // Dispatch events first (event-driven architecture)
        eventSystem.dispatch();
        eventSystem.clear();

        // Update all managers independently (placeholder for Phase 1)
        // battleManager.update(delta);
        // economyManager.update(delta);
        // cardManager.update(delta);
        // inputHandler.update(delta);
    }

    @Override
    public void render(RenderHolder holder) {
        // Render all managers in layer order (placeholder for Phase 1)
        // battleManager.render(holder);
        // cardManager.render(holder);
        // holder.flush();

        // Economy manager typically doesn't render
        // economyManager.render(holder);

        // Or use RenderCoordinator
        // renderCoordinator.renderAll();
    }

    @Override
    public void handleInput(InputContext context) {
        // Delegate to input handler for proper event dispatch
        inputHandler.handleInput(context);
    }

    @Override
    public void pause() {
        // Pause all managers (placeholder for Phase 1)
        // battleManager.pause();
        // economyManager.pause();
        // cardManager.pause();
        // inputHandler.pause();
    }

    @Override
    public void resume() {
        // Resume all managers (placeholder for Phase 1)
        // battleManager.resume();
        // economyManager.resume();
        // cardManager.resume();
        // inputHandler.resume();
    }

    @Override
    public void onExit() {
        // Exit all managers (placeholder for Phase 1)
        // battleManager.onExit();
        // economyManager.onExit();
        // cardManager.onExit();
        // inputHandler.onExit();
    }

    @Override
    public void dispose() {
        // Unregister event listener (not needed for Phase 1)
        // eventSystem.unregisterListener(this);

        // Dispose all managers (placeholder for Phase 1)
        // battleManager.dispose();
        // economyManager.dispose();
        // cardManager.dispose();
        // inputHandler.dispose();
    }

    // Accessors for managers (used by InputHandler, to be implemented in later phases)
    /*
    public BattleManager getBattleManager() {
        return battleManager;
    }

    public CardManager getCardManager() {
        return cardManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    */

    // Accessor for battle state
    public BattleState getBattleState() {
        return battleState;
    }
}
