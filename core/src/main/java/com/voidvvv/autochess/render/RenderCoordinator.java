package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Render coordination class
 * Holds RenderHolder and manages render order for all GameRenderer implementations
 *
 * IMPORTANT: Each manager manages its own viewport and begin/end calls
 * RenderCoordinator only ensures flush() is called between managers
 */
public class RenderCoordinator {
    private final RenderHolder holder;
    private final List<GameRenderer> renderers;

    public RenderCoordinator(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        this.holder = new RenderHolder(spriteBatch, shapeRenderer);
        this.renderers = new ArrayList<>();
    }

    /**
     * Add a renderer to the render pipeline
     * @param renderer GameRenderer implementation
     */
    public void addRenderer(GameRenderer renderer) {
        renderers.add(renderer);
    }

    /**
     * Remove a renderer from the render pipeline
     * @param renderer GameRenderer implementation
     */
    public void removeRenderer(GameRenderer renderer) {
        renderers.remove(renderer);
    }

    /**
     * Get the RenderHolder for custom rendering needs
     * @return RenderHolder with SpriteBatch and ShapeRenderer
     */
    public RenderHolder getHolder() {
        return holder;
    }

    /**
     * Render all registered renderers in layer order
     * IMPORTANT: Flush is called after each manager to ensure state consistency
     */
    public void renderAll() {
        // Clear screen should be done before this method
        // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for (GameRenderer renderer : renderers) {
            renderer.render(holder);
            holder.flush();  // Ensure state consistency between managers
        }
    }

    /**
     * Dispose of render resources
     */
    public void dispose() {
        // RenderHolder doesn't own SpriteBatch/ShapeRenderer
        // They should be disposed by GameScreen
    }
}
