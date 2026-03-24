package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Centralized rendering context
 * Holds SpriteBatch and ShapeRenderer references for all rendering operations
 *
 * IMPORTANT: Viewport management is handled by individual managers, not by RenderHolder
 * IMPORTANT: Each manager manages its own viewport and begin/end calls
 */
public class RenderHolder {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;

    public RenderHolder(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    /**
     * Flush both spriteBatch and shapeRenderer to ensure state consistency between managers
     * CRITICAL: Must be called before switching between SpriteBatch and ShapeRenderer
     * CRITICAL: Must be called when manager rendering is complete
     */
    public void flush() {
        spriteBatch.flush();
        // ShapeRenderer flush - prevents state corruption when switching renderers
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.flush();
        }
    }

    /**
     * Check if either renderer is currently active (drawing)
     * Used for validation and debugging
     */
    public boolean isActive() {
        return spriteBatch.isDrawing() || shapeRenderer.isDrawing();
    }

    public void dispose() {
        this.spriteBatch.dispose();
        this.getShapeRenderer().dispose();
    }
}
