package com.voidvvv.autochess.model;

/**
 * 活跃效果
 * 从事件中获得，在整局 Roguelike 游戏中持续生效
 */
public class ActiveEffect {
    private final String sourceEventId;
    private final String sourceEventTitle;
    private final EventEffectType effectType;
    private final float value;

    public ActiveEffect(String sourceEventId, String sourceEventTitle, EventEffectType effectType, float value) {
        this.sourceEventId = sourceEventId;
        this.sourceEventTitle = sourceEventTitle;
        this.effectType = effectType;
        this.value = value;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public String getSourceEventTitle() {
        return sourceEventTitle;
    }

    public EventEffectType getEffectType() {
        return effectType;
    }

    public float getValue() {
        return value;
    }

    /**
     * 获取效果描述文本
     */
    public String getDescription() {
        switch (effectType) {
            case ATTACK_BOOST:
                return sourceEventTitle + ": 攻击力 +" + (int)(value * 100) + "%";
            case HEALTH_BOOST:
                return sourceEventTitle + ": 生命值 +" + (int)(value * 100) + "%";
            case DEFENSE_BOOST:
                return sourceEventTitle + ": 防御力 +" + (int)(value * 100) + "%";
            case GOLD:
                return sourceEventTitle + ": 金币 +" + (int)value;
            case FREE_CARD:
                return sourceEventTitle + ": 免费卡牌";
            case HEAL:
                return sourceEventTitle + ": 回复生命";
            default:
                return sourceEventTitle;
        }
    }
}
