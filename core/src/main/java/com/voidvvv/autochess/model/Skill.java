package com.voidvvv.autochess.model;

/**
 * 技能接口 - 使用泛型避免跨层依赖
 * 遵循策略模式，实现开闭原则
 *
 * @param <T> 上下文对象类型，由具体技能实现决定需要的类型
 */
public interface Skill<T> {
    /**
     * 获取技能名称
     */
    String getName();

    /**
     * 释放技能
     * @param context 上下文对象，包含战斗相关信息
     */
    void cast(T context);
}
