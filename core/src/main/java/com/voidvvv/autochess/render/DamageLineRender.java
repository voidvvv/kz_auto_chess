package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.DamageShowModel;
import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.utils.FontUtils;

public class DamageLineRender {
    private ModelHolder<DamageShowModel> damageShowModelModelHolder;

    public DamageLineRender(ModelHolder<DamageShowModel> damageShowModelModelHolder) {
        this.damageShowModelModelHolder = damageShowModelModelHolder;
    }

    public void render (ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (DamageShowModel model : damageShowModelModelHolder.getModels()) {
            Damage damage = model.damage;
            BattleCharacter from = model.from;
            BattleCharacter to = model.to;
            Color lineColor = model.damage.critical? Color.RED:Color.WHITE;
            shapeRenderer.rectLine(from.getX(), from.getY(), to.getX(), to.getY(), 3f);
        }
        shapeRenderer.end();
        BitmapFont defaultFont = FontUtils.getDefaultFont();
        spriteBatch.begin();
        for (DamageShowModel model : damageShowModelModelHolder.getModels()) {
            BattleCharacter to = model.to;
            defaultFont.draw(spriteBatch, (int)model.damage.val + "", to.getX(), to.getY());
        }

        spriteBatch.end();

    }
}
