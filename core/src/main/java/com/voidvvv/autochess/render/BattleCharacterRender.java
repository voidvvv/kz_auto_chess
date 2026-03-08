package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.utils.CharacterRenderer;
import com.voidvvv.autochess.utils.RenderConfig;

public class BattleCharacterRender {


    public static void render(SpriteBatch spriteBatch, BattleCharacter character) {
        if (!RenderConfig.USE_TILED_RENDERING) {
            return;
        }
        if (character.isDead()) {
            // 渲染死亡角色为半透明，表示已死亡但会在下一轮复活
            TiledBattleCharacterRender.renderWithAlpha(spriteBatch, character, 0.3f);
        } else {
            TiledBattleCharacterRender.render(spriteBatch, character);
        }

    }

    public static void render(ShapeRenderer shapeRenderer, BattleCharacter character) {
        CharacterRenderer.render(shapeRenderer, character);
    }
}
