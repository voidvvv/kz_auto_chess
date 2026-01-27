package io.github.some_example_name.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.KzAutoChess;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.Battlefield;
import io.github.some_example_name.utils.CharacterRenderer;
import io.github.some_example_name.utils.FontUtils;

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
        // 绘制战场背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.15f, 0.2f, 1);
        shapeRenderer.rect(battlefield.getX(), battlefield.getY(), battlefield.getWidth(), battlefield.getHeight());
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(battlefield.getX(), battlefield.getY(), battlefield.getWidth(), battlefield.getHeight());
        shapeRenderer.end();

        // 绘制标题
        game.getBatch().begin();
        BitmapFont font = FontUtils.getSmallFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);
        GlyphLayout titleLayout = new GlyphLayout(font, "战场");
        // Y坐标：从战场区域的顶部向下偏移（世界坐标系统Y轴向上）
        float titleY = battlefield.getY() +  battlefield.getHeight() - 10;
        font.draw(game.getBatch(), titleLayout, battlefield.getX() + 10, titleY);
        game.getBatch().end();

        // 绘制战场上的角色
        for (BattleCharacter character : battlefield.getCharacters()) {
            CharacterRenderer.render(shapeRenderer, character);
        }
    }
}
