package com.voidvvv.autochess.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

/**
 * Unified input context with proper coordinate system handling
 * Provides both screen coordinates and world coordinates to avoid confusion
 */
public class InputContext {
    /** Screen coordinates (from Gdx.input.getX()/getY()) */
    public final float screenX;
    public final float screenY;

    /** World coordinates (unprojected by camera) */
    public final float worldX;
    public final float worldY;

    /** Touch/mouse state */
    public final boolean justTouched;
    public final int pointer;
    public final int button;

    /** Input source for debugging */
    public final InputType type;

    public enum InputType {
        TOUCH, MOUSE
    }

    public InputContext(float screenX, float screenY,
                     float worldX, float worldY,
                     boolean justTouched, int pointer, int button,
                     InputType type) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.worldX = worldX;
        this.worldY = worldY;
        this.justTouched = justTouched;
        this.pointer = pointer;
        this.button = button;
        this.type = type;
    }

    /**
     * Factory method to create InputContext from LibGDX Input
     * @param camera Camera for world coordinate projection
     * @return InputContext with both coordinate systems
     */
    public static InputContext fromInput(com.badlogic.gdx.graphics.Camera camera) {
        float screenX = Gdx.input.getX();
        float screenY = Gdx.input.getY();
        Vector3 worldPos = camera.unproject(new Vector3(screenX, screenY, 0));

        boolean justTouched = Gdx.input.justTouched();
        InputType type = Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)
                ? InputType.TOUCH : InputType.MOUSE;

        return new InputContext(screenX, screenY,
                            worldPos.x, worldPos.y,
                            justTouched, -1, -1,
                            type);
    }
}
