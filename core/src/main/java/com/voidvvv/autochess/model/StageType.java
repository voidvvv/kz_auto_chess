package com.voidvvv.autochess.model;

/**
 * 关卡类型枚举
 * 用于 Roguelike 模式中区分不同类型的关卡
 */
public enum StageType {
    /**
     * 普通关 - 标准战斗
     */
    NORMAL,

    /**
     * Boss关 - 1-2个高属性敌人
     */
    BOSS,

    /**
     * 事件关 - 战斗胜利后触发随机事件
     */
    EVENT
}
