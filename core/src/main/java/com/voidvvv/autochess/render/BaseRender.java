package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface BaseRender {
    void render(SpriteBatch spriteBatch);

    void render(ShapeRenderer shapeRenderer);
}
