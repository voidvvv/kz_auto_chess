package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;
import com.voidvvv.autochess.model.SkillType;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌数据类
 */
public class Card {
    private int id;
    private String name;
    private String description;
    private int cost; // 购买费用
    private int tier; // 卡牌等级/稀有度（1-5）
    private CardType type; // 卡牌类型
    private int starLevel; // 星级（1-3）
    private int baseCardId; // 基础卡牌ID，用于追踪升级链
    private List<SynergyType> synergies; // 羁绊类型列表
    private String tiledResourceKey; // Tiled资源key（格式: "tilesetId+tileId"）
    private SkillType skillType; // 技能类型
    private float maxMana; // 魔法值上限覆盖

    public Card(int id, String name, String description, int cost, int tier, CardType type) {
        this(id, name, description, cost, tier, type, 1, id, new ArrayList<>());
    }

    public Card(int id, String name, String description, int cost, int tier, CardType type,
                int starLevel, int baseCardId, List<SynergyType> synergies) {
        this(id, name, description, cost, tier, type, starLevel, baseCardId, synergies, null);
        this.tiledResourceKey = null;
    }

    public Card(int id, String name, String description, int cost, int tier, CardType type,
                int starLevel, int baseCardId, List<SynergyType> synergies, String tiledResourceKey) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.tier = tier;
        this.type = type;
        this.starLevel = starLevel;
        this.baseCardId = baseCardId;
        this.synergies = synergies != null ? synergies : new ArrayList<>();
        this.tiledResourceKey = tiledResourceKey;
        this.skillType = null; // 默认为null
        this.maxMana = 0f; // 默认为0，使用CharacterStats的maxMana
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getTier() {
        return tier;
    }

    public CardType getType() {
        return type;
    }

    public int getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(int starLevel) {
        this.starLevel = starLevel;
    }

    public int getBaseCardId() {
        return baseCardId;
    }

    public void setBaseCardId(int baseCardId) {
        this.baseCardId = baseCardId;
    }

    public List<SynergyType> getSynergies() {
        return synergies;
    }

    public void setSynergies(List<SynergyType> synergies) {
        this.synergies = synergies;
    }

    public void addSynergy(SynergyType synergy) {
        if (this.synergies == null) {
            this.synergies = new ArrayList<>();
        }
        this.synergies.add(synergy);
    }

    public boolean hasSynergy(SynergyType synergy) {
        return this.synergies != null && this.synergies.contains(synergy);
    }

    public String getTiledResourceKey() {
        return tiledResourceKey;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public void setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }

    public float getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(float maxMana) {
        this.maxMana = maxMana;
    }

    /**
     * 卡牌类型枚举
     */
    public enum CardType {
        WARRIOR("warrior"),
        MAGE("mage"),
        ARCHER("archer"),
        ASSASSIN("assassin"),
        TANK("tank");

        private final String i18nKey;

        CardType(String i18nKey) {
            this.i18nKey = i18nKey;
        }

        public String getDisplayName() {
            String key = "card_type_" + i18nKey;
            return I18N.get(key, getDefaultDisplayName());
        }

        private String getDefaultDisplayName() {
            switch (this) {
                case WARRIOR: return "战士";
                case MAGE: return "法师";
                case ARCHER: return "射手";
                case ASSASSIN: return "刺客";
                case TANK: return "坦克";
                default: return name();
            }
        }
    }
}
