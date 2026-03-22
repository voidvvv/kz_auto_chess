package com.voidvvv.autochess.model;

/**
 * 事件选项
 * 玩家可以从多个选项中选择一个
 */
public class EventChoice {
    private final String text;
    private final EventEffectType effectType;
    private final float value;

    public EventChoice(String text, EventEffectType effectType, float value) {
        this.text = text;
        this.effectType = effectType;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public EventEffectType getEffectType() {
        return effectType;
    }

    public float getValue() {
        return value;
    }
}
