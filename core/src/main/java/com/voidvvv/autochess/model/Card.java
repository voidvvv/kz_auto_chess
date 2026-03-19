package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;
import com.voidvvv.autochess.model.SkillType;
import com.voidvvv.autochess.model.battle.Damage;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌数据类
 * Represents a card in the auto-chess game with associated skills and properties.
 *
 * <p>This class follows Java Bean conventions with proper encapsulation,
 * validation, and immutable collection handling where appropriate.</p>
 */
public class Card {
    // Default skill parameter values
    private static final float DEFAULT_SKILL_VALUE = 50f;
    private static final float DEFAULT_SKILL_RANGE = 100f;
    private static final float DEFAULT_SKILL_DURATION = 5f;

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

    // New skill parameters
    private float skillValue; // 技能数值（伤害/治疗量/增益值）
    private float skillRange; // 技能范围（像素/世界单位）
    private float skillDuration; // 技能持续时间（秒，用于BUFF/DEBUFF）
    private Damage.DamageType skillDamageType; // 技能伤害类型

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

        // Initialize skill parameters with defaults
        this.skillValue = DEFAULT_SKILL_VALUE;
        this.skillRange = DEFAULT_SKILL_RANGE;
        this.skillDuration = DEFAULT_SKILL_DURATION;
        this.skillDamageType = Damage.DamageType.PhySic;
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

    // ==================== Skill Parameter Methods ====================

    /**
     * Gets the skill value parameter.
     * Represents the primary value of the skill (damage, heal amount, etc.).
     *
     * @return the skill value
     */
    public float getSkillValue() {
        return skillValue;
    }

    /**
     * Sets the skill value parameter.
     *
     * @param skillValue the skill value to set
     * @throws IllegalArgumentException if skillValue is negative
     */
    public void setSkillValue(float skillValue) {
        if (skillValue < 0f) {
            throw new IllegalArgumentException("Skill value cannot be negative: " + skillValue);
        }
        this.skillValue = skillValue;
    }

    /**
     * Gets the skill range parameter.
     * Represents the maximum distance for skill effects.
     *
     * @return the skill range in world units
     */
    public float getSkillRange() {
        return skillRange;
    }

    /**
     * Sets the skill range parameter.
     *
     * @param skillRange the skill range to set
     * @throws IllegalArgumentException if skillRange is negative
     */
    public void setSkillRange(float skillRange) {
        if (skillRange < 0f) {
            throw new IllegalArgumentException("Skill range cannot be negative: " + skillRange);
        }
        this.skillRange = skillRange;
    }

    /**
     * Gets the skill duration parameter.
     * Represents how long temporary effects (BUFF/DEBUFF) last.
     *
     * @return the skill duration in seconds
     */
    public float getSkillDuration() {
        return skillDuration;
    }

    /**
     * Sets the skill duration parameter.
     *
     * @param skillDuration the skill duration to set
     * @throws IllegalArgumentException if skillDuration is negative
     */
    public void setSkillDuration(float skillDuration) {
        if (skillDuration < 0f) {
            throw new IllegalArgumentException("Skill duration cannot be negative: " + skillDuration);
        }
        this.skillDuration = skillDuration;
    }

    /**
     * Gets the skill damage type parameter.
     * Determines the damage type for damage-dealing skills.
     *
     * @return the skill damage type
     */
    public Damage.DamageType getSkillDamageType() {
        return skillDamageType;
    }

    /**
     * Sets the skill damage type parameter.
     *
     * @param skillDamageType the skill damage type to set
     * @throws IllegalArgumentException if skillDamageType is null
     */
    public void setSkillDamageType(Damage.DamageType skillDamageType) {
        if (skillDamageType == null) {
            throw new IllegalArgumentException("Skill damage type cannot be null");
        }
        this.skillDamageType = skillDamageType;
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
