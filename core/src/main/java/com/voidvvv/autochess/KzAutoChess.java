package com.voidvvv.autochess;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.voidvvv.autochess.screens.StartScreen;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;
import com.voidvvv.autochess.utils.ViewManagement;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.model.SharedCardPool;
import com.voidvvv.autochess.battle.PlayerLifeBlackboard;

/**
 * 自走棋游戏主类
 */
public class KzAutoChess extends Game {
    private SpriteBatch batch;
    private ViewManagement viewManagement;
    private GamePhase gamePhase = GamePhase.PLACEMENT;
    private PlayerLifeBlackboard playerLifeBlackboard;
    private SharedCardPool sharedCardPool;

    public KzAutoChess() {
        viewManagement = new ViewManagement();
    }

    public ViewManagement getViewManagement() {
        return viewManagement;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    /**
     * 获取玩家血量黑板
     */
    public PlayerLifeBlackboard getPlayerLifeBlackboard() {
        return playerLifeBlackboard;
    }

    /**
     * 设置玩家血量黑板（用于关卡间共享）
     */
    public void setPlayerLifeBlackboard(PlayerLifeBlackboard playerLifeBlackboard) {
        this.playerLifeBlackboard = playerLifeBlackboard;
    }

    /**
     * 获取共享卡池
     */
    public SharedCardPool getSharedCardPool() {
        return sharedCardPool;
    }

    /**
     * 设置共享卡池（用于新游戏初始化）
     */
    public void setSharedCardPool(SharedCardPool sharedCardPool) {
        this.sharedCardPool = sharedCardPool;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        // 在游戏启动时初始化字体
        FontUtils.init();
        // 初始化i18n系统
        I18N.init();
        // 初始化玩家血量黑板
        playerLifeBlackboard = new PlayerLifeBlackboard();
        // 初始化共享卡池
        sharedCardPool = new SharedCardPool();
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

