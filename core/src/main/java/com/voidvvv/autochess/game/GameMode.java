package com.voidvvv.autochess.game;

import com.voidvvv.autochess.input.InputContext;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * Game mode abstraction - decouples Screen from game logic
 * Enables easy extension for different game types (AutoChess, Tutorial, Survival, etc.)
 */
public interface GameMode {

    /**
     * Called when entering this game mode
     */
    void onEnter();

    /**
     * Update game logic
     * @param delta Time elapsed since last frame in seconds
     */
    void update(float delta);

    /**
     * Render game content
     * @param holder Rendering context with SpriteBatch and ShapeRenderer
     */
    void render(RenderHolder holder);

    /**
     * Handle input events with unified context
     * InputContext provides both screen and world coordinates
     * Managers can choose which coordinate system to use based on their needs
     */
    void handleInput(InputContext context);

    /**
     * Pause game logic
     */
    void pause();

    /**
     * Resume game logic
     */
    void resume();

    /**
     * Called when exiting this game mode
     */
    void onExit();

    /**
     * Cleanup resources
     */
    void dispose();
}
