package com.voidvvv.autochess.model;

/**
 * 技能类型枚举
 * 用于Card中配置角色技能类型
 */
public enum SkillType {
    /**
     * 基础技能（控制台打印）
     */
    BASIC,

    /**
     * 治疗技能（未来扩展）
     */
    HEAL,

    /**
     * 范围伤害技能（未来扩展）
     */
    AOE,

    /**
     * 增益技能（未来扩展）
     */
    BUFF,

    /**
     * 减益技能（未来扩展）
     */
    DEBUFF
}
