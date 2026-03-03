package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.utils.CharacterRenderer;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

public class BattleFieldRender {
    private KzAutoChess game;
    private ShapeRenderer shapeRenderer;

    public BattleFieldRender(ShapeRenderer shapeRenderer,KzAutoChess game) {
        this.game = game;
        this.shapeRenderer = shapeRenderer;
    }

    public void render(Battlefield battlefield) {
        shapeRenderer.setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
        drawBattlefield(battlefield);
    }

    private void drawBattlefield(Battlefield battlefield) {
        float x = battlefield.getX();
        float y = battlefield.getY();
        float w = battlefield.getWidth();
        float h = battlefield.getHeight();
        float splitY = battlefield.getPlayerZoneTop();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.18f, 0.25f, 1);
        shapeRenderer.rect(x, y, w, splitY - y);
        shapeRenderer.setColor(0.18f, 0.12f, 0.2f, 1);
        shapeRenderer.rect(x, splitY, w, y + h - splitY);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.setColor(0.6f, 0.6f, 0.8f, 1);
        shapeRenderer.line(x, splitY, x + w, splitY);
        shapeRenderer.end();

        game.getBatch().begin();
        BitmapFont font = FontUtils.getSmallFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);
        GlyphLayout titleLayout = new GlyphLayout(font, I18N.get("battlefield"));
        float titleY = battlefield.getY() + battlefield.getHeight() - 10;
        font.draw(game.getBatch(), titleLayout, battlefield.getX() + 10, titleY);
        font.setColor(0.7f, 0.8f, 1f, 1);
        GlyphLayout playerLayout = new GlyphLayout(font, I18N.get("player_side"));
        font.draw(game.getBatch(), playerLayout, x + 10, splitY - 25);
        font.setColor(0.9f, 0.6f, 0.6f, 1);
        GlyphLayout enemyLayout = new GlyphLayout(font, I18N.get("enemy_side"));
        font.draw(game.getBatch(), enemyLayout, x + 10, splitY + 15);
        game.getBatch().end();

        for (BattleCharacter character : battlefield.getCharacters()) {
            if (!character.isDead()) {
                CharacterRenderer.render(shapeRenderer, character);
            }
        }
    }
}
