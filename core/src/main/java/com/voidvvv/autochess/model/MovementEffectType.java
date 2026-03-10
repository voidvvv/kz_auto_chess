package com.voidvvv.autochess.model;

/**
 * 移动效果类型枚举
 * 定义四种移动效果类型及其优先级
 */
public enum MovementEffectType {
    /**
     * 拖拽效果 - 施加外部速度（击退、拉拽）
     * 叠加规则: 向量叠加
     */
    DRAG(1),

    /**
     * 禁锢效果 - 阻止自身移动（眩晕、定身）
     * 叠加规则: 取最长持续时间
     */
    IMMOBILIZE(4),

    /**
     * 强制速度 - 覆盖移动速度（冲刺、传送带）
     * 叠加规则: 取最高优先级
     */
    FIXED_VELOCITY(3),

    /**
     * 速度修正 - 百分比调整速度（加速、减速）
     * 叠加规则: 乘法叠加
     */
    SPEED_MODIFIER(2);

    private final int priority;

    MovementEffectType(int priority) {
        this.priority = priority;
    }

    /**
     * 获取效果类型的优先级
     * 数值越高优先级越高
     * @return 优先级值
     */
    public int getPriority() {
        return priority;
    }
}
