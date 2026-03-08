package com.voidvvv.autochess.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.voidvvv.autochess.model.CharacterStats;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色属性加载器
 * 负责从JSON文件加载角色属性配置
 * 从模型中分离资源加载逻辑，使模型保持框架无关
 */
public class CharacterStatsLoader {

    private final JsonReader jsonReader;
    private final Map<Integer, CharacterStats> statsMap;
    private boolean loaded;

    public CharacterStatsLoader() {
        this.jsonReader = new JsonReader();
        this.statsMap = new HashMap<>();
        this.loaded = false;
    }

    /**
     * 加载配置文件
     * @param configPath 配置文件路径（相对于assets目录）
     */
    public void load(String configPath) {
        if (loaded) return;

        try {
            FileHandle file = Gdx.files.internal(configPath);
            JsonValue json = jsonReader.parse(file);

            JsonValue characters = json.get("characters");
            if (characters != null) {
                for (JsonValue character : characters) {
                    int cardId = character.getInt("cardId");
                    int health = character.getInt("health");
                    int mana = character.getInt("mana");
                    int attack = character.getInt("attack");
                    int defense = character.getInt("defense");
                    int magicPower = character.getInt("magicPower");
                    int magicResist = character.getInt("magicResist");
                    int agility = character.getInt("agility");

                    CharacterStats stats = new CharacterStats(cardId, health, mana, attack, defense,
                                                                  magicPower, magicResist, agility);
                    statsMap.put(cardId, stats);
                }
            }

            loaded = true;
            Gdx.app.log("CharacterStatsLoader", "Loaded " + statsMap.size() + " character stats");
        } catch (Exception e) {
            Gdx.app.error("CharacterStatsLoader", "Failed to load character stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据卡牌ID获取角色属性
     * @param cardId 卡牌ID
     * @return 角色属性，如果不存在则返回null
     */
    public CharacterStats getStats(int cardId) {
        if (!loaded) {
            load("character_stats.json");
        }
        return statsMap.get(cardId);
    }

    /**
     * 检查是否已加载
     * @return 如果已加载则返回true
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 重新加载配置
     */
    public void reload() {
        loaded = false;
        statsMap.clear();
        load("character_stats.json");
    }

    /**
     * 清除缓存
     */
    public void clear() {
        statsMap.clear();
        loaded = false;
    }

    /**
     * 获取所有已加载的角色属性
     * @return 角色属性映射的副本
     */
    public Map<Integer, CharacterStats> getAllStats() {
        return new HashMap<>(statsMap);
    }
}
