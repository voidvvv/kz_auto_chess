package com.voidvvv.autochess.model.effect;

import com.badlogic.gdx.graphics.Color;

/**
 * 技能效果类型枚举
 * 定义不同技能类型的视觉效果分类
 */
public enum SkillEffectType {
    /**
     * 治疗效果 - 绿色
     */
    HEAL(new Color(0.2f, 1f, 0.2f, 0.8f)),

    /**
     * 范围伤害效果 - 橙色
     */
    AOE(new Color(1f, 0.5f, 0f, 0.6f)),

    /**
     * 增益效果 - 青色
     */
    BUFF(new Color(0f, 1f, 1f, 0.7f)),

    /**
     * 减益效果 - 紫色
     */
    DEBUFF(new Color(0.6f, 0f, 0.8f, 0.7f)),

    /**
     * 基础效果 - 白色（默认回退）
     */
    BASIC(Color.WHITE);

    private final Color defaultColor;

    SkillEffectType(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    /**
     * 获取该效果类型的默认颜色
     * @return 默认颜色
     */
    public Color getDefaultColor() {
        return defaultColor;
    }
}
