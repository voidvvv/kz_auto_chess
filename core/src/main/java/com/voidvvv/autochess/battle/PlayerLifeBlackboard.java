package com.voidvvv.autochess.battle;

import com.voidvvv.autochess.logic.LifeConfig;
import com.voidvvv.autochess.model.PlayerLifeModel;

/**
 * 玩家血量黑板
 *
 * 聚合同一个游戏会话内的玩家血量状态，可跨关卡共享
 * 负责处理战斗结束后的血量结算逻辑
 */
public class PlayerLifeBlackboard {
    private PlayerLifeModel lifeModel;
    private final LifeConfig config;

    // 当前关卡追踪（用于显示）
    private int currentLevel = 1;
    private int maxReachedLevel = 1;

    /**
     * 使用默认配置创建黑板
     */
    public PlayerLifeBlackboard() {
        this.config = LifeConfig.load();
        this.lifeModel = new PlayerLifeModel(config.getInitialHealth());
    }

    /**
     * 使用指定配置创建黑板
     */
    public PlayerLifeBlackboard(LifeConfig config) {
        this.config = config;
        this.lifeModel = new PlayerLifeModel(config.getInitialHealth());
    }

    /**
     * 使用指定血量模型创建黑板（用于测试）
     */
    public PlayerLifeBlackboard(PlayerLifeModel lifeModel, LifeConfig config) {
        this.lifeModel = lifeModel;
        this.config = config;
    }

    /**
     * 处理战斗结束事件
     * @param playerWon 是否胜利
     * @param remainingEnemies 剩余敌人数量
     */
    public void onBattleEnd(boolean playerWon, int remainingEnemies) {
        if (playerWon) {
            // 胜利：血量不变
            // 更新到达的最高关卡
            if (currentLevel > maxReachedLevel) {
                maxReachedLevel = currentLevel;
            }
        } else {
            // 失败：根据剩余敌人扣血
            int damage = calculateDamage(remainingEnemies);
            if (damage > 0) {
                PlayerLifeModel previousState = lifeModel;
                lifeModel = lifeModel.takeDamage(damage);
            }
        }
    }

    /**
     * 计算失败时的伤害值
     * @param remainingEnemies 剩余敌人数量
     * @return 伤害值
     */
    private int calculateDamage(int remainingEnemies) {
        return remainingEnemies * config.getDamagePerEnemy();
    }

    /**
     * 检查游戏是否结束（玩家死亡）
     */
    public boolean isGameOver() {
        return lifeModel.isDead();
    }

    /**
     * 重置血量到初始值（用于新游戏）
     */
    public void reset() {
        lifeModel = lifeModel.reset(config.getInitialHealth());
        currentLevel = 1;
        maxReachedLevel = 1;
    }

    /**
     * 设置当前关卡
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    /**
     * 获取当前关卡
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 获取到达的最高关卡
     */
    public int getMaxReachedLevel() {
        return maxReachedLevel;
    }

    /**
     * 获取血量模型（用于渲染）
     */
    public PlayerLifeModel getLifeModel() {
        return lifeModel;
    }

    /**
     * 获取当前血量
     */
    public int getCurrentHealth() {
        return lifeModel.getCurrentHealth();
    }

    /**
     * 获取最大血量
     */
    public int getMaxHealth() {
        return lifeModel.getMaxHealth();
    }

    /**
     * 获取血量百分比
     */
    public float getHealthPercentage() {
        return lifeModel.getHealthPercentage();
    }

    /**
     * 获取配置
     */
    public LifeConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return String.format("PlayerLifeBlackboard{lifeModel=%s, currentLevel=%d, maxReachedLevel=%d}",
                lifeModel, currentLevel, maxReachedLevel);
    }
}
