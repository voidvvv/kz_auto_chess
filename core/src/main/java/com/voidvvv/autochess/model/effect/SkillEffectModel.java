package com.voidvvv.autochess.model.effect;

import com.voidvvv.autochess.model.BattleCharacter;

/**
 * 技能效果数据模型
 * 不可变设计，通过 withXxx() 方法创建新实例
 */
public final class SkillEffectModel {
    private final String id;
    private final SkillEffectType type;
    private final BattleCharacter caster;
    private final BattleCharacter target;
    private final String skillName;
    private final float worldX;
    private final float worldY;
    private final float range;
    private final float duration;
    private final float startTime;
    private final float elapsed;

    /**
     * 完整构造函数
     */
    public SkillEffectModel(String id, SkillEffectType type, BattleCharacter caster,
                           BattleCharacter target, String skillName,
                           float worldX, float worldY, float range,
                           float duration, float startTime, float elapsed) {
        this.id = id;
        this.type = type;
        this.caster = caster;
        this.target = target;
        this.skillName = skillName;
        this.worldX = worldX;
        this.worldY = worldY;
        this.range = range;
        this.duration = duration;
        this.startTime = startTime;
        this.elapsed = elapsed;
    }

    /**
     * 简化构造函数（初始 elapsed = 0）
     */
    public SkillEffectModel(String id, SkillEffectType type, BattleCharacter caster,
                           BattleCharacter target, String skillName,
                           float worldX, float worldY, float range,
                           float duration, float startTime) {
        this(id, type, caster, target, skillName, worldX, worldY, range, duration, startTime, 0f);
    }

    // ========== Getters ==========

    public String getId() {
        return id;
    }

    public SkillEffectType getType() {
        return type;
    }

    public BattleCharacter getCaster() {
        return caster;
    }

    public BattleCharacter getTarget() {
        return target;
    }

    public String getSkillName() {
        return skillName;
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public float getRange() {
        return range;
    }

    public float getDuration() {
        return duration;
    }

    public float getStartTime() {
        return startTime;
    }

    public float getElapsed() {
        return elapsed;
    }

    // ========== 不可变更新方法 ==========

    /**
     * 创建带有新 elapsed 值的副本
     * @param newElapsed 新的已过时间
     * @return 新的 SkillEffectModel 实例
     */
    public SkillEffectModel withElapsed(float newElapsed) {
        return new SkillEffectModel(id, type, caster, target, skillName,
                                    worldX, worldY, range, duration, startTime, newElapsed);
    }

    // ========== 状态查询 ==========

    /**
     * 检查效果是否已过期
     * @return 如果已过持续时间返回 true
     */
    public boolean isExpired() {
        return elapsed >= duration;
    }

    /**
     * 获取效果进度（0.0 - 1.0）
     * @return 进度比例
     */
    public float getProgress() {
        return Math.min(1f, elapsed / duration);
    }

    /**
     * 获取剩余时间
     * @return 剩余时间（秒）
     */
    public float getRemainingTime() {
        return Math.max(0f, duration - elapsed);
    }

    @Override
    public String toString() {
        return "SkillEffectModel{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", skillName='" + skillName + '\'' +
                ", progress=" + getProgress() +
                '}';
    }
}
