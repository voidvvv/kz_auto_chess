package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.model.RoguelikeConfig;
import com.voidvvv.autochess.model.StageType;

/**
 * 阶段/关卡管理器
 * 管理关卡进度和类型判断
 */
public class StageManager {
    private final RoguelikeConfig config;
    private int currentStage;

    public StageManager(RoguelikeConfig config) {
        this.config = config;
        this.currentStage = 1;
    }

    /**
     * 获取当前关卡
     */
    public int getCurrentStage() {
        return currentStage;
    }

    /**
     * 获取最大关卡数
     */
    public int getMaxStages() {
        return config.getMaxStages();
    }

    /**
     * 进入下一关
     * @return 如果超过最大关卡数则返回 false
     */
    public boolean nextStage() {
        int oldStage = currentStage;
        if (currentStage >= config.getMaxStages()) {
            Gdx.app.log("StageManager", "Already at max stage " + currentStage);
            return false;
        }
        currentStage++;
        Gdx.app.log("StageManager", "Stage advanced from " + oldStage + " to " + currentStage);
        return true;
    }

    /**
     * 获取当前关卡类型
     */
    public StageType getStageType() {
        return getStageType(currentStage);
    }

    /**
     * 获取指定关卡类型
     */
    public StageType getStageType(int stage) {
        if (config.isBossStage(stage)) {
            return StageType.BOSS;
        } else if (config.isEventStage(stage)) {
            return StageType.EVENT;
        } else {
            return StageType.NORMAL;
        }
    }

    /**
     * 重置到第一关
     */
    public void reset() {
        currentStage = 1;
    }

    /**
     * 检查是否完成所有关卡
     */
    public boolean isCompleted() {
        return currentStage > config.getMaxStages();
    }

    /**
     * 获取当前关卡的奖励金币
     */
    public int getCurrentStageReward() {
        return config.getStageReward(currentStage);
    }
}
