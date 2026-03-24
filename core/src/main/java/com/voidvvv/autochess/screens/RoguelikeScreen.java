package com.voidvvv.autochess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputMultiplexer;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.game.RoguelikeGameMode;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * Roguelike 模式屏幕
 *
 * 职责：
 * - Screen 层面的生命周期管理
 * - 创建和管理 RenderHolder（复用 game.getBatch()）
 * - 设置 InputMultiplexer（UI Stage + InputHandlerV2）
 * - 委托游戏逻辑给 RoguelikeGameMode
 */
public class RoguelikeScreen implements Screen {
    private RenderHolder renderHolder;
    private final KzAutoChess game;
    private final RoguelikeGameMode roguelikeGameMode;
    private InputMultiplexer inputMultiplexer;
    RenderCoordinator renderCoordinator;

    public RoguelikeScreen(KzAutoChess game) {
        this.game = game;


        // 1. 创建 RenderHolder（复用 game 的 SpriteBatch）
        this.renderHolder = new RenderHolder(game.getBatch(), new ShapeRenderer());
        this.renderCoordinator = new RenderCoordinator(this.renderHolder.getSpriteBatch(),this.renderHolder.getShapeRenderer());


        this.roguelikeGameMode = new RoguelikeGameMode(game, this.renderCoordinator);
    }

    @Override
    public void show() {


        // 3. 启动 GameMode
        roguelikeGameMode.onEnter();

        // 2. 设置输入处理器
        setupInput();

    }

    /**
     * 设置输入处理器
     * 优先级：UI Stage > InputHandlerV2 > 全局快捷键
     */
    private void setupInput() {
        inputMultiplexer = new InputMultiplexer();

        // 优先级1: UI Stage（按钮点击等）
        if (roguelikeGameMode.getUIStage() != null) {
            inputMultiplexer.addProcessor(roguelikeGameMode.getUIStage());
        }

        // 优先级2: InputHandlerV2（游戏世界交互）
        if (roguelikeGameMode.getInputHandler() != null) {
            inputMultiplexer.addProcessor(roguelikeGameMode.getInputHandler());
        }

        // 优先级3: 全局快捷键
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    // 返回关卡选择界面
                    game.setScreen(new LevelSelectScreen(game));
                    return true;
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // 1. 清空屏幕
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. 更新 GameMode（包含所有 Manager 的 update）
        roguelikeGameMode.update(delta);

        // 3. 渲染 GameMode（包含所有 GameRenderer 的 render）
        renderCoordinator.renderAll();
    }

    @Override
    public void resize(int width, int height) {

        game.getViewManagement().update(width, height);
    }

    @Override
    public void pause() {
        roguelikeGameMode.pause();
    }

    @Override
    public void resume() {
        roguelikeGameMode.resume();
    }

    @Override
    public void hide() {
        // 清理输入处理器
        if (inputMultiplexer != null) {
            inputMultiplexer.clear();
            Gdx.input.setInputProcessor(null);
            inputMultiplexer = null;
        }

        // 退出 GameMode
        roguelikeGameMode.onExit();
    }

    @Override
    public void dispose() {
        if (renderHolder != null) {
            // 注意：不 dispose SpriteBatch，因为它是从 game 复用的
            // 只 dispose ShapeRenderer
            if (renderHolder.getShapeRenderer() != null) {
                renderHolder.getShapeRenderer().dispose();
            }
            renderHolder = null;
        }

        if (roguelikeGameMode != null) {
            roguelikeGameMode.dispose();
        }
    }
}
