package com.voidvvv.autochess.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Roguelike 模式配置
 * 从 JSON 文件加载配置参数
 */
public class RoguelikeConfig {
    private int maxStages = 30;
    private int initialGold = 5;
    private int normalStageReward = 5;
    private int bossStageReward = 10;
    private int[] eventStages = {3, 8, 13, 18, 23, 28};
    private int[] bossStages = {5, 10, 15, 20, 25, 30};
    private float statScaling = 0.05f;

    /**
     * 从 JSON 文件加载配置
     * @param path JSON 文件路径（相对于 assets 目录）
     * @return RoguelikeConfig 实例，如果加载失败则返回默认配置
     */
    public static RoguelikeConfig loadFromJson(String path) {
        RoguelikeConfig config = new RoguelikeConfig();
        try {
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                JsonValue root = new JsonReader().parse(file);
                config.maxStages = root.getInt("maxStages", 30);
                config.initialGold = root.getInt("initialGold", 5);
                config.normalStageReward = root.getInt("normalStageReward", 5);
                config.bossStageReward = root.getInt("bossStageReward", 10);

                // 加载事件关配置
                JsonValue eventStagesValue = root.get("eventStages");
                if (eventStagesValue != null && eventStagesValue.isArray()) {
                    config.eventStages = new int[eventStagesValue.size];
                    for (int i = 0; i < eventStagesValue.size; i++) {
                        config.eventStages[i] = eventStagesValue.get(i).asInt();
                    }
                }

                // 加载 Boss 关配置
                JsonValue bossStagesValue = root.get("bossStages");
                if (bossStagesValue != null && bossStagesValue.isArray()) {
                    config.bossStages = new int[bossStagesValue.size];
                    for (int i = 0; i < bossStagesValue.size; i++) {
                        config.bossStages[i] = bossStagesValue.get(i).asInt();
                    }
                }

                config.statScaling = root.getFloat("statScaling", 0.05f);
                Gdx.app.log("RoguelikeConfig", "Config loaded from " + path);
            } else {
                Gdx.app.error("RoguelikeConfig", "Config file not found: " + path + ", using defaults");
            }
        } catch (Exception e) {
            Gdx.app.error("RoguelikeConfig", "Failed to load config, using defaults", e);
        }
        return config;
    }

    // Getters
    public int getMaxStages() { return maxStages; }
    public int getInitialGold() { return initialGold; }
    public int getNormalStageReward() { return normalStageReward; }
    public int getBossStageReward() { return bossStageReward; }
    public int[] getEventStages() { return eventStages; }
    public int[] getBossStages() { return bossStages; }
    public float getStatScaling() { return statScaling; }

    /**
     * 检查指定关卡是否为事件关
     */
    public boolean isEventStage(int stage) {
        for (int s : eventStages) {
            if (s == stage) return true;
        }
        return false;
    }

    /**
     * 检查指定关卡是否为 Boss 关
     */
    public boolean isBossStage(int stage) {
        for (int s : bossStages) {
            if (s == stage) return true;
        }
        return false;
    }

    /**
     * 获取指定关卡的奖励金币
     */
    public int getStageReward(int stage) {
        return isBossStage(stage) ? bossStageReward : normalStageReward;
    }
}
