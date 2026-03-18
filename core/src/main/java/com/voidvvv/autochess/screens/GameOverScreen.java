package com.voidvvv.autochess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

/**
 * 游戏结束界面
 * 当玩家血量归零时显示
 */
public class GameOverScreen implements Screen {
    private final KzAutoChess game;
    private final int maxReachedLevel;

    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private ShapeRenderer shapeRenderer;

    private float mainMenuX, mainMenuY, mainMenuWidth, mainMenuHeight;

    public GameOverScreen(KzAutoChess game, int maxReachedLevel) {
        this.game = game;
        this.maxReachedLevel = maxReachedLevel;

        this.titleFont = FontUtils.getLargeFont();
        this.buttonFont = FontUtils.getDefaultFont();
        this.shapeRenderer = new ShapeRenderer();

        initButtons();
    }

    private void initButtons() {
        mainMenuWidth = 200;
        mainMenuHeight = 60;
        mainMenuX = (Gdx.graphics.getWidth() - mainMenuWidth) / 2;
        mainMenuY = Gdx.graphics.getHeight() / 2 - mainMenuHeight / 2;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // 检查鼠标是否在按钮上
        boolean hover = mouseX >= mainMenuX && mouseX <= mainMenuX + mainMenuWidth &&
                       mouseY >= mainMenuY && mouseY <= mainMenuY + mainMenuHeight;

        // 启用混合模式
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 绘制主菜单按钮
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (hover) {
            shapeRenderer.setColor(0.4f, 0.3f, 0.5f, 1);
        } else {
            shapeRenderer.setColor(0.3f, 0.2f, 0.4f, 1);
        }
        shapeRenderer.rect(mainMenuX, mainMenuY, mainMenuWidth, mainMenuHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(mainMenuX, mainMenuY, mainMenuWidth, mainMenuHeight);
        shapeRenderer.end();

        game.getBatch().begin();

        // 绘制"游戏结束"标题
        if (titleFont == null) {
            Gdx.app.error("GameOverScreen", "Title font is null!");
            titleFont = new BitmapFont();
        }

        titleFont.setColor(new Color(0.9f, 0.2f, 0.2f, 1));
        titleFont.getData().setScale(2.0f);
        String gameOverText = I18N.get("game_over");
        GlyphLayout gameOverLayout = new GlyphLayout(titleFont, gameOverText);
        float gameOverX = (Gdx.graphics.getWidth() - gameOverLayout.width) / 2;
        float gameOverY = Gdx.graphics.getHeight() * 0.7f;
        titleFont.draw(game.getBatch(), gameOverLayout, gameOverX, gameOverY);

        // 绘制到达的关卡信息
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.0f);
        String levelText = I18N.format("max_level_reached", maxReachedLevel);
        GlyphLayout levelLayout = new GlyphLayout(titleFont, levelText);
        float levelX = (Gdx.graphics.getWidth() - levelLayout.width) / 2;
        float levelY = gameOverY - 80;
        titleFont.draw(game.getBatch(), levelLayout, levelX, levelY);

        // 绘制按钮文字
        buttonFont.setColor(Color.WHITE);
        buttonFont.getData().setScale(1.0f);
        String buttonText = I18N.get("main_menu");
        GlyphLayout buttonLayout = new GlyphLayout(buttonFont, buttonText);
        float textX = mainMenuX + (mainMenuWidth - buttonLayout.width) / 2;
        float textY = mainMenuY + (mainMenuHeight + buttonLayout.height) / 2;
        buttonFont.draw(game.getBatch(), buttonLayout, textX, textY);

        game.getBatch().end();

        // 处理点击
        if (Gdx.input.justTouched() && hover) {
            game.setScreen(new StartScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        mainMenuX = (width - mainMenuWidth) / 2;
        mainMenuY = height / 2 - mainMenuHeight / 2;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
