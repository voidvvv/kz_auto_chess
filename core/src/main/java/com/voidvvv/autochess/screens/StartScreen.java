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

/**
 * 游戏开始界面
 */
public class StartScreen implements Screen {
    private KzAutoChess game;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private ShapeRenderer shapeRenderer;
    private float buttonX, buttonY, buttonWidth, buttonHeight;

    public StartScreen(KzAutoChess game) {
        this.game = game;
        // 字体已在游戏主类中初始化，这里直接获取
        this.titleFont = FontUtils.getLargeFont();
        this.buttonFont = FontUtils.getDefaultFont();
        this.shapeRenderer = new ShapeRenderer();

        // 计算按钮位置（居中）
        buttonWidth = 200;
        buttonHeight = 60;
        buttonX = (Gdx.graphics.getWidth() - buttonWidth) / 2;
        buttonY = Gdx.graphics.getHeight() / 2 - buttonHeight / 2;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // 检查鼠标是否在按钮上
        boolean hover = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                       mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

        // 先绘制所有ShapeRenderer内容
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (hover) {
            shapeRenderer.setColor(0.3f, 0.5f, 0.8f, 1);
        } else {
            shapeRenderer.setColor(0.2f, 0.4f, 0.7f, 1);
        }
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        // 绘制按钮边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        // 确保ShapeRenderer完全结束后再开始SpriteBatch
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 绘制文字
        game.getBatch().begin();

        // 测试：先绘制一个简单的英文文字，确保字体系统工作
        if (titleFont == null) {
            Gdx.app.error("StartScreen", "Title font is null!");
            titleFont = new BitmapFont();
        }
        if (buttonFont == null) {
            Gdx.app.error("StartScreen", "Button font is null!");
            buttonFont = new BitmapFont();
        }

        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.0f);

        // 绘制标题
        GlyphLayout titleLayout = new GlyphLayout(titleFont, "自走棋");
        float titleX = (Gdx.graphics.getWidth() - titleLayout.width) / 2;
        float titleY = Gdx.graphics.getHeight() - 100;

        // 如果布局宽度为0，说明字体不支持这些字符，使用英文
        if (titleLayout.width < 1) {
            titleLayout = new GlyphLayout(titleFont, "Auto Chess");
            titleX = (Gdx.graphics.getWidth() - titleLayout.width) / 2;
        }
        titleFont.draw(game.getBatch(), titleLayout, titleX, titleY);

        // 绘制按钮文字
        buttonFont.setColor(Color.WHITE);
        buttonFont.getData().setScale(1.0f);
        GlyphLayout buttonLayout = new GlyphLayout(buttonFont, "开始游戏");
        float textX = buttonX + (buttonWidth - buttonLayout.width) / 2;
        float textY = buttonY + (buttonHeight + buttonLayout.height) / 2;

        // 如果布局宽度为0，使用英文
        if (buttonLayout.width < 1) {
            buttonLayout = new GlyphLayout(buttonFont, "Start Game");
            textX = buttonX + (buttonWidth - buttonLayout.width) / 2;
        }
        buttonFont.draw(game.getBatch(), buttonLayout, textX, textY);

        game.getBatch().end();

        // 处理点击
        if (Gdx.input.justTouched() && hover) {
            game.setScreen(new LevelSelectScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        buttonX = (width - buttonWidth) / 2;
        buttonY = height / 2 - buttonHeight / 2;
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

