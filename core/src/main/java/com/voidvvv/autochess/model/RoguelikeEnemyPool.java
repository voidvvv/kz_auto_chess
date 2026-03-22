package com.voidvvv.autochess.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Roguelike 敌人池
 * 从 JSON 配置加载敌人池，根据关卡随机生成敌人队伍
 */
public class RoguelikeEnemyPool {
    private Map<Integer, TierConfig> tierConfigs;
    private Map<String, Integer> stageTierMap;
    private Random random;

    /**
     * 梯队配置
     */
    private static class TierConfig {
        int[] cardIds;
        int minCount;
        int maxCount;

        TierConfig(int[] cardIds, int minCount, int maxCount) {
            this.cardIds = cardIds;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

    public RoguelikeEnemyPool() {
        this.tierConfigs = new HashMap<>();
        this.stageTierMap = new HashMap<>();
        this.random = new Random();
    }

    /**
     * 从 JSON 文件加载配置
     */
    public void loadFromJson(String path) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (!file.exists()) {
                Gdx.app.error("RoguelikeEnemyPool", "Config file not found: " + path);
                loadDefaultConfig();
                return;
            }

            JsonValue root = new JsonReader().parse(file);

            // 加载梯队配置
            JsonValue tiers = root.get("tiers");
            if (tiers != null) {
                for (JsonValue tierConfig : tiers) {
                    // tierConfig.name 获取 JSON 对象的键名
                    String tierKey = tierConfig.name;
                    if (tierKey == null || tierKey.isEmpty()) {
                        // 如果没有 name，尝试使用索引
                        continue;
                    }
                    JsonValue cardIdsValue = tierConfig.get("cardIds");
                    int[] cardIds = new int[cardIdsValue.size];
                    for (int i = 0; i < cardIdsValue.size; i++) {
                        cardIds[i] = cardIdsValue.get(i).asInt();
                    }
                    int minCount = tierConfig.getInt("minCount", 3);
                    int maxCount = tierConfig.getInt("maxCount", 4);
                    tierConfigs.put(Integer.parseInt(tierKey), new TierConfig(cardIds, minCount, maxCount));
                }
            }

            // 加载关卡-梯队映射
            JsonValue stageMap = root.get("stageTierMap");
            if (stageMap != null) {
                for (JsonValue tierValue : stageMap) {
                    String rangeKey = tierValue.name;
                    if (rangeKey == null || rangeKey.isEmpty()) {
                        continue;
                    }
                    int tier = tierValue.asInt();
                    stageTierMap.put(rangeKey, tier);
                }
            }

            Gdx.app.log("RoguelikeEnemyPool", "Loaded " + tierConfigs.size() + " tier configs");
        } catch (Exception e) {
            Gdx.app.error("RoguelikeEnemyPool", "Failed to load config, using defaults", e);
            loadDefaultConfig();
        }
    }

    /**
     * 加载默认配置
     */
    private void loadDefaultConfig() {
        tierConfigs.put(1, new TierConfig(new int[]{140, 141, 142, 143}, 3, 4));
        tierConfigs.put(2, new TierConfig(new int[]{140, 141, 142, 143, 144}, 3, 4));
        tierConfigs.put(3, new TierConfig(new int[]{141, 142, 143, 144, 145}, 3, 5));
        tierConfigs.put(4, new TierConfig(new int[]{145, 146, 147, 148, 149}, 2, 4));
        tierConfigs.put(5, new TierConfig(new int[]{148, 149, 150, 151}, 1, 2));

        stageTierMap.put("1-6", 1);
        stageTierMap.put("7-12", 2);
        stageTierMap.put("13-18", 3);
        stageTierMap.put("19-24", 4);
        stageTierMap.put("25-30", 5);
    }

    /**
     * 获取指定关卡对应的梯队
     */
    public int getTierForStage(int stage) {
        for (Map.Entry<String, Integer> entry : stageTierMap.entrySet()) {
            String range = entry.getKey();
            int tier = entry.getValue();
            String[] parts = range.split("-");
            if (parts.length == 2) {
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                if (stage >= min && stage <= max) {
                    return tier;
                }
            }
        }
        return 1; // 默认返回梯队 1
    }

    /**
     * 获取指定关卡的敌人卡牌 ID 列表
     * @param stage 关卡数
     * @param stageType 关卡类型
     * @param statScaling 属性缩放系数（用于 Boss 关）
     * @return 敌人卡牌 ID 列表
     */
    public List<Integer> getEnemyCardIds(int stage, StageType stageType, float statScaling) {
        List<Integer> result = new ArrayList<>();

        if (stageType == StageType.BOSS) {
            // Boss 关：从当前梯队或更高梯队选择 1-2 个敌人
            int tier = Math.min(getTierForStage(stage) + 1, 5);
            TierConfig config = tierConfigs.get(tier);
            if (config == null) config = tierConfigs.get(5);

            int bossCount = 1 + (random.nextInt(2)); // 1-2 个 Boss
            for (int i = 0; i < bossCount && i < config.cardIds.length; i++) {
                int index = random.nextInt(config.cardIds.length);
                result.add(config.cardIds[index]);
            }
        } else {
            // 普通关/事件关：从对应梯队随机选择敌人
            int tier = getTierForStage(stage);
            TierConfig config = tierConfigs.get(tier);
            if (config == null) {
                config = tierConfigs.get(1);
            }

            int count = config.minCount + random.nextInt(config.maxCount - config.minCount + 1);
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(config.cardIds.length);
                result.add(config.cardIds[index]);
            }
        }

        return result;
    }

    /**
     * 应用属性缩放到角色属性
     * @param baseStats 基础属性
     * @param stage 关卡数
     * @param scaling 缩放系数
     * @return 缩放后的属性
     */
    public CharacterStats applyStatScaling(CharacterStats baseStats, int stage, float scaling) {
        // 缩放公式：属性 = 基础属性 × (1 + (关卡数 - 1) × 缩放系数)
        float multiplier = 1 + (stage - 1) * scaling;

        // CharacterStats 的构造函数: (cardId, health, mana, attack, defense, magicPower, magicResist, agility)
        return new CharacterStats(
                baseStats.getCardId(),
                baseStats.getHealth() * multiplier,
                baseStats.getMana(),
                baseStats.getAttack() * multiplier,
                baseStats.getDefense() * multiplier,
                baseStats.getMagicPower() * multiplier,
                baseStats.getMagicResist() * multiplier,
                baseStats.getAgility()
        );
    }

    /**
     * 设置随机种子（用于测试）
     */
    public void setSeed(long seed) {
        this.random = new Random(seed);
    }
}
