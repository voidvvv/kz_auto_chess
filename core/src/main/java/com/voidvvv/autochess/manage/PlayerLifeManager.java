package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.PlayerLifeBlackboard;
import com.voidvvv.autochess.event.BattleEndEvent;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.PlayerDeathEvent;
import com.voidvvv.autochess.event.PlayerLifeChangedEvent;

/**
 * PlayerLifeManager - 管理玩家血量系统
 *
 * 职责:
 * - 响应战斗结束事件 (BattleEndEvent)
 * - 计算并应用血量变化
 * - 发送血量变化事件 (PlayerLifeChangedEvent)
 * - 发送玩家死亡事件 (PlayerDeathEvent)
 * - 管理血量系统的生命周期
 */
public class PlayerLifeManager implements GameEventListener {

    private final PlayerLifeBlackboard blackboard;
    private final GameEventSystem eventSystem;

    public PlayerLifeManager(PlayerLifeBlackboard blackboard, GameEventSystem eventSystem) {
        this.blackboard = blackboard;
        this.eventSystem = eventSystem;
    }

    // ========== Lifecycle Methods ==========

    public void onEnter() {
        eventSystem.registerListener(this);
        Gdx.app.log("PlayerLifeManager", "Initialized with config: " + blackboard.getConfig());
    }

    public void pause() {
        // 无需暂停逻辑
    }

    public void resume() {
        // 无需恢复逻辑
    }

    public void onExit() {
        eventSystem.unregisterListener(this);
    }

    public void dispose() {
        // 无需释放资源
    }

    // ========== Event Handling ==========

    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof BattleEndEvent) {
            handleBattleEnd((BattleEndEvent) event);
        }
    }

    /**
     * 处理战斗结束事件
     */
    private void handleBattleEnd(BattleEndEvent event) {
        int previousHealth = blackboard.getCurrentHealth();
        boolean playerWon = event.playerWon;
        int remainingEnemies = event.remainingEnemies;

        // 让黑板处理血量结算
        blackboard.onBattleEnd(playerWon, remainingEnemies);

        int newHealth = blackboard.getCurrentHealth();
        int damageTaken = previousHealth - newHealth;

        // 发送血量变化事件（如果有变化）
        if (damageTaken > 0) {
            eventSystem.postEvent(new PlayerLifeChangedEvent(
                    previousHealth, newHealth, damageTaken, blackboard.getLifeModel().isDead()
            ));
            Gdx.app.log("PlayerLifeManager", String.format(
                    "Battle ended: %s, Damage taken: %d, Health: %d -> %d",
                    playerWon ? "WIN" : "LOSE", damageTaken, previousHealth, newHealth
            ));
        }

        // 检查游戏是否结束
        if (blackboard.isGameOver()) {
            eventSystem.postEvent(new PlayerDeathEvent(
                    blackboard.getCurrentHealth(),
                    blackboard.getMaxReachedLevel()
            ));
            Gdx.app.log("PlayerLifeManager", "Player died! Max level reached: " +
                    blackboard.getMaxReachedLevel());
        }
    }

    // ========== Public API ==========

    /**
     * 获取血量黑板
     */
    public PlayerLifeBlackboard getBlackboard() {
        return blackboard;
    }

    /**
     * 获取当前血量
     */
    public int getCurrentHealth() {
        return blackboard.getCurrentHealth();
    }

    /**
     * 获取最大血量
     */
    public int getMaxHealth() {
        return blackboard.getMaxHealth();
    }

    /**
     * 检查游戏是否结束
     */
    public boolean isGameOver() {
        return blackboard.isGameOver();
    }

    /**
     * 设置当前关卡
     */
    public void setCurrentLevel(int level) {
        blackboard.setCurrentLevel(level);
    }

    /**
     * 获取当前关卡
     */
    public int getCurrentLevel() {
        return blackboard.getCurrentLevel();
    }
}
