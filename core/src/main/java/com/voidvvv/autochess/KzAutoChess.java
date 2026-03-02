package com.voidvvv.autochess;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.voidvvv.autochess.screens.StartScreen;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.ViewManagement;

/**
 * 自走棋游戏主类
 */
public class KzAutoChess extends Game {
    private SpriteBatch batch;
    private ViewManagement viewManagement;

    public KzAutoChess() {
        viewManagement = new ViewManagement();
    }

    public ViewManagement getViewManagement() {
        return viewManagement;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        // 在游戏启动时初始化字体
        FontUtils.init();
        viewManagement.create();
        // 设置初始界面为开始界面
        setScreen(new StartScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (screen != null) {
            screen.dispose();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}

