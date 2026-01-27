package io.github.some_example_name.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色属性配置类
 * 用于加载和解析角色属性配置文件
 */
public class CharacterStats {
    private int cardId;
    private int health;        // 健康值
    private int mana;          // 法力值
    private int attack;        // 攻击力
    private int defense;       // 防御力
    private int magicPower;    // 魔法强度
    private int magicResist;   // 魔法抵抗
    private int agility;       // 敏捷值
    
    public CharacterStats(int cardId, int health, int mana, int attack, int defense, 
                         int magicPower, int magicResist, int agility) {
        this.cardId = cardId;
        this.health = health;
        this.mana = mana;
        this.attack = attack;
        this.defense = defense;
        this.magicPower = magicPower;
        this.magicResist = magicResist;
        this.agility = agility;
    }
    
    // Getters
    public int getCardId() { return cardId; }
    public int getHealth() { return health; }
    public int getMana() { return mana; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getMagicPower() { return magicPower; }
    public int getMagicResist() { return magicResist; }
    public int getAgility() { return agility; }
    
    /**
     * 角色属性配置管理器
     */
    public static class Config {
        private static Map<Integer, CharacterStats> statsMap = new HashMap<>();
        private static boolean loaded = false;
        
        /**
         * 加载配置文件
         */
        public static void load() {
            if (loaded) return;
            
            try {
                JsonReader jsonReader = new JsonReader();
                JsonValue json = jsonReader.parse(Gdx.files.internal("character_stats.json"));
                
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
                Gdx.app.log("CharacterStats", "Loaded " + statsMap.size() + " character stats");
            } catch (Exception e) {
                Gdx.app.error("CharacterStats", "Failed to load character stats: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        /**
         * 根据卡牌ID获取角色属性
         */
        public static CharacterStats getStats(int cardId) {
            if (!loaded) {
                load();
            }
            return statsMap.get(cardId);
        }
        
        /**
         * 检查是否已加载
         */
        public static boolean isLoaded() {
            return loaded;
        }
        
        /**
         * 重新加载配置
         */
        public static void reload() {
            loaded = false;
            statsMap.clear();
            load();
        }
    }
}

