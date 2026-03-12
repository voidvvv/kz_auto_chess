package com.voidvvv.autochess.render;

/**
 * Unified rendering interface for all managers that need to render
 * Managers implement this interface to participate in rendering pipeline
 */
public interface GameRenderer {

    /**
     * Render content using provided rendering context
     * @param holder Contains SpriteBatch and ShapeRenderer
     *
     * IMPORTANT: Each manager must manage its own begin/end calls
     * IMPORTANT: Each manager should call holder.flush() when done
     */
    void render(RenderHolder holder);
}
