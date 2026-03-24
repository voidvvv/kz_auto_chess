package com.voidvvv.autochess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.battle.PlayerLifeBlackboard;
import com.voidvvv.autochess.render.LifeBarRenderer;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

/**
 * 关卡选择界面
 */
public class LevelSelectScreen implements Screen {
    private KzAutoChess game;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private ShapeRenderer shapeRenderer;

    private static final int LEVEL_COUNT = 5;
    private float[] levelButtonX;
    private float[] levelButtonY;
    private float levelButtonWidth = 120;
    private float levelButtonHeight = 60;
    private float backButtonX, backButtonY, backButtonWidth = 150, backButtonHeight = 50;

    // 无尽模式按钮
    private float roguelikeButtonX, roguelikeButtonY, roguelikeButtonWidth = 200, roguelikeButtonHeight = 60;

    public LevelSelectScreen(KzAutoChess game) {
        this.game = game;
        // 字体已在游戏主类中初始化，这里直接获取
        this.titleFont = FontUtils.getLargeFont();
        this.buttonFont = FontUtils.getDefaultFont();
        this.shapeRenderer = new ShapeRenderer();

        initButtons();
    }

    private void initButtons() {
        float startX = (Gdx.graphics.getWidth() - (LEVEL_COUNT * (levelButtonWidth + 20))) / 2;
        float y = Gdx.graphics.getHeight() / 2;

        levelButtonX = new float[LEVEL_COUNT];
        levelButtonY = new float[LEVEL_COUNT];

        for (int i = 0; i < LEVEL_COUNT; i++) {
            levelButtonX[i] = startX + i * (levelButtonWidth + 20);
            levelButtonY[i] = y;
        }

        // 无尽模式按钮 - 在关卡按钮下方居中
        roguelikeButtonX = (Gdx.graphics.getWidth() - roguelikeButtonWidth) / 2;
        roguelikeButtonY = y - levelButtonHeight - 40;

        backButtonX = 50;
        backButtonY = Gdx.graphics.getHeight() - 70;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // 启用混合模式
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 绘制标题
        game.getBatch().begin();
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.0f);
        GlyphLayout titleLayout = new GlyphLayout(titleFont, I18N.get("level_select"));
        float titleX = (Gdx.graphics.getWidth() - titleLayout.width) / 2;
        float titleY = Gdx.graphics.getHeight() - 80;
        titleFont.draw(game.getBatch(), titleLayout, titleX, titleY);
        game.getBatch().end();

        // 绘制血条（在关卡选择界面上方）
        PlayerLifeBlackboard playerLife = game.getPlayerLifeBlackboard();
        if (playerLife != null && !playerLife.getLifeModel().isDead()) {
            float lifeBarX = 50;
            float lifeBarY = titleY - 50;
            float lifeBarWidth = 200;
            float lifeBarHeight = 20;
            LifeBarRenderer.render(shapeRenderer, game.getBatch(),
                    playerLife.getLifeModel(),
                    lifeBarX, lifeBarY, lifeBarWidth, lifeBarHeight, titleFont);
        }

        // 绘制关卡按钮
        for (int i = 0; i < LEVEL_COUNT; i++) {
            boolean hover = mouseX >= levelButtonX[i] && mouseX <= levelButtonX[i] + levelButtonWidth &&
                           mouseY >= levelButtonY[i] && mouseY <= levelButtonY[i] + levelButtonHeight;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (hover) {
                shapeRenderer.setColor(0.4f, 0.6f, 0.3f, 1);
            } else {
                shapeRenderer.setColor(0.3f, 0.5f, 0.2f, 1);
            }
            shapeRenderer.rect(levelButtonX[i], levelButtonY[i], levelButtonWidth, levelButtonHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(levelButtonX[i], levelButtonY[i], levelButtonWidth, levelButtonHeight);
            shapeRenderer.end();

            game.getBatch().begin();
            buttonFont.setColor(Color.WHITE);
            buttonFont.getData().setScale(1.0f);
            String levelText = I18N.format("stage_level", (i + 1));
            GlyphLayout layout = new GlyphLayout(buttonFont, levelText);
            float textX = levelButtonX[i] + (levelButtonWidth - layout.width) / 2;
            float textY = levelButtonY[i] + (levelButtonHeight + layout.height) / 2;
            buttonFont.draw(game.getBatch(), layout, textX, textY);
            game.getBatch().end();

            if (Gdx.input.justTouched() && hover) {
                game.setScreen(new GameScreen(game, i + 1));
                return;
            }
        }

        // 绘制无尽模式按钮
        boolean roguelikeHover = mouseX >= roguelikeButtonX && mouseX <= roguelikeButtonX + roguelikeButtonWidth &&
                                 mouseY >= roguelikeButtonY && mouseY <= roguelikeButtonY + roguelikeButtonHeight;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (roguelikeHover) {
            shapeRenderer.setColor(0.6f, 0.4f, 0.8f, 1);  // 紫色高亮
        } else {
            shapeRenderer.setColor(0.5f, 0.3f, 0.7f, 1);  // 紫色
        }
        shapeRenderer.rect(roguelikeButtonX, roguelikeButtonY, roguelikeButtonWidth, roguelikeButtonHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(roguelikeButtonX, roguelikeButtonY, roguelikeButtonWidth, roguelikeButtonHeight);
        shapeRenderer.end();

        game.getBatch().begin();
        buttonFont.setColor(Color.WHITE);
        buttonFont.getData().setScale(1.0f);
        String roguelikeText = I18N.get("roguelike.title", "无尽模式");
        GlyphLayout roguelikeLayout = new GlyphLayout(buttonFont, roguelikeText);
        float roguelikeTextX = roguelikeButtonX + (roguelikeButtonWidth - roguelikeLayout.width) / 2;
        float roguelikeTextY = roguelikeButtonY + (roguelikeButtonHeight + roguelikeLayout.height) / 2;
        buttonFont.draw(game.getBatch(), roguelikeLayout, roguelikeTextX, roguelikeTextY);
        game.getBatch().end();

        if (Gdx.input.justTouched() && roguelikeHover) {
            game.setScreen(new RoguelikeScreen(game));
        }

        // 绘制返回按钮
        boolean backHover = mouseX >= backButtonX && mouseX <= backButtonX + backButtonWidth &&
                            mouseY >= backButtonY && mouseY <= backButtonY + backButtonHeight;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (backHover) {
            shapeRenderer.setColor(0.6f, 0.3f, 0.3f, 1);
        } else {
            shapeRenderer.setColor(0.5f, 0.2f, 0.2f, 1);
        }
        shapeRenderer.rect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        shapeRenderer.end();

        game.getBatch().begin();
        buttonFont.setColor(Color.WHITE);
        buttonFont.getData().setScale(1.0f);
        GlyphLayout backLayout = new GlyphLayout(buttonFont, I18N.get("back"));
        float backTextX = backButtonX + (backButtonWidth - backLayout.width) / 2;
        float backTextY = backButtonY + (backButtonHeight + backLayout.height) / 2;
        buttonFont.draw(game.getBatch(), backLayout, backTextX, backTextY);
        game.getBatch().end();

        if (Gdx.input.justTouched() && backHover) {
            game.setScreen(new StartScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        initButtons();
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

