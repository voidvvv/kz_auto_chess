package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.screens.StartScreen;
import io.github.some_example_name.utils.FontUtils;
import io.github.some_example_name.utils.ViewManagement;

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

