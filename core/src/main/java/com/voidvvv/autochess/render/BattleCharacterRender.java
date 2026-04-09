package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.manage.CharacterEffectManager;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.utils.CharacterRenderer;
import com.voidvvv.autochess.utils.RenderConfig;
import com.voidvvv.autochess.render.ManaBarRenderer;
import com.voidvvv.autochess.render.HealthBarRenderer;

public class BattleCharacterRender {

    public static void render(SpriteBatch spriteBatch, BattleCharacter character, CharacterEffectManager characterEffectManager) {
        if (character.isDead()) {
            TiledBattleCharacterRender.renderWithAlpha(spriteBatch, character, 0.3f, characterEffectManager);
        } else {
            TiledBattleCharacterRender.render(spriteBatch, character, characterEffectManager);
        }
    }

    public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
        CharacterRenderer.render(shapeRenderer, blackboard.getSelf());
        HealthBarRenderer.render(shapeRenderer, blackboard);
        ManaBarRenderer.render(shapeRenderer, blackboard);
    }
}
