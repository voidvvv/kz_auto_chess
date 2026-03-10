package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;

/**
 * 移动效果数据类（纯数据容器）
 * 用于表示施加在角色上的各种移动效果
 */
public class MovementEffect {
    // 唯一标识
    private String effectId;

    // 效果来源（用于按来源清除）
    private String sourceId;

    // 效果类型
    private MovementEffectType type;

    // 速度向量（用于 DRAG/FIXED_VELOCITY）
    private Vector2 velocity = new Vector2();

    // 速度修正系数（用于 SPEED_MODIFIER，1.0=无变化，0.5=减速50%）
    private float speedModifier = 1.0f;

    // 持续时间（秒，-1表示永久，0表示瞬发）
    private float duration = 0f;
    private float remainingDuration = 0f;

    // 效果优先级（同类型内的优先级）
    private int priority = 0;

    // 衰减系数（每秒衰减比例，0=不衰减）
    private float decay = 0f;

    // Getters and Setters
    public String getEffectId() { return effectId; }
    public void setEffectId(String effectId) { this.effectId = effectId; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public MovementEffectType getType() { return type; }
    public void setType(MovementEffectType type) { this.type = type; }

    public Vector2 getVelocity() { return velocity; }
    public void setVelocity(Vector2 velocity) { this.velocity.set(velocity); }

    public float getSpeedModifier() { return speedModifier; }
    public void setSpeedModifier(float speedModifier) { this.speedModifier = speedModifier; }

    public float getDuration() { return duration; }
    public void setDuration(float duration) {
        this.duration = duration;
        this.remainingDuration = duration;
    }

    public float getRemainingDuration() { return remainingDuration; }
    public void setRemainingDuration(float remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public float getDecay() { return decay; }
    public void setDecay(float decay) { this.decay = decay; }

    /**
     * 检查效果是否已过期
     * @return 如果效果已过期返回true
     */
    public boolean isExpired() {
        return duration > 0 && remainingDuration <= 0;
    }

    /**
     * 检查效果是否为永久效果
     * @return 如果是永久效果返回true
     */
    public boolean isPermanent() {
        return duration < 0;
    }

    /**
     * 重置效果状态（用于对象池重用）
     */
    public void reset() {
        this.effectId = null;
        this.sourceId = null;
        this.type = null;
        this.velocity.set(0, 0);
        this.speedModifier = 1.0f;
        this.duration = 0f;
        this.remainingDuration = 0f;
        this.priority = 0;
        this.decay = 0f;
    }
}
