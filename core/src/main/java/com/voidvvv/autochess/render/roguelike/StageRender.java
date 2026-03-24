package com.voidvvv.autochess.render.roguelike;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.voidvvv.autochess.render.GameRenderer;
import com.voidvvv.autochess.render.RenderHolder;

public class StageRender implements GameRenderer {
    Stage stage;

    public StageRender(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void render(RenderHolder holder) {
        this.stage.draw();
    }
}
