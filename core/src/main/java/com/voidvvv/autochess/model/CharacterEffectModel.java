package com.voidvvv.autochess.model;

import com.badlogic.gdx.graphics.Color;
import java.util.Objects;

/**
 * 角色视觉效果数据模型（纯数据容器，不包含逻辑）
 * 效果仅影响渲染，不影响碰撞/判定
 */
public class CharacterEffectModel {
    /**
     * 视觉效果类型
     */
    public enum EffectType {
        SHAKE,      // 攻击动画：水平抖动
        DASH_UP,    // 技能动画：向上移动后返回原位
        FLASH_RED    // 受伤动画：红色闪烁
    }

    private final EffectType type;
    private final int targetCharacterId;  // BattleCharacter的ID (int类型)
    private final long startTime;
    private final float duration;
    private float progress;  // 0.0 到 1.0
    private boolean isActive;

    // DASH_UP 特定字段
    private final float dashDistance;
    private float originalY;

    // FLASH_RED 特定字段
    private final Color flashColor;

    /**
     * 构造函数
     * @param type 效果类型
     * @param targetCharacterId 目标角色ID
     * @param duration 持续时间（秒）
     */
    public CharacterEffectModel(EffectType type, int targetCharacterId, float duration) {
        this(type, targetCharacterId, duration, 0f, 0f, Color.RED.cpy());
    }

    /**
     * 私有构造函数（用于builder模式）
     */
    private CharacterEffectModel(EffectType type, int targetCharacterId, float duration,
                              float dashDistance, float originalY, Color flashColor) {
        this.type = type;
        this.targetCharacterId = targetCharacterId;
        this.startTime = System.currentTimeMillis();
        this.duration = duration * 1000f;
        this.progress = 0f;
        this.isActive = true;
        this.dashDistance = dashDistance;
        this.originalY = originalY;
        this.flashColor = flashColor;
    }

    /**
     * 设置突进距离（DASH_UP 效果用）
     * @param distance 突进像素距离
     */
    public CharacterEffectModel withDashDistance(float distance) {
        return new CharacterEffectModel(type, targetCharacterId, duration / 1000f, distance, originalY, flashColor);
    }

    /**
     * 设置闪烁颜色（FLASH_RED 效果用）
     * @param color 闪烁颜色
     */
    public CharacterEffectModel withFlashColor(Color color) {
        return new CharacterEffectModel(type, targetCharacterId, duration / 1000f, dashDistance, originalY, color.cpy());
    }

    /**
     * 设置原始Y坐标（DASH_UP 效果用）
     * @param originalY 原始Y坐标
     */
    public void setOriginalY(float originalY) {
        this.originalY = originalY;
    }

    // ========== Getters ==========

    public EffectType getType() {
        return type;
    }

    public int getTargetCharacterId() {
        return targetCharacterId;
    }

    public long getStartTime() {
        return startTime;
    }

    public float getDuration() {
        return duration;
    }

    public float getProgress() {
        return progress;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getDashDistance() {
        return dashDistance;
    }

    public float getOriginalY() {
        return originalY;
    }

    public Color getFlashColor() {
        return flashColor;
    }

    // ========== Setters ==========

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    // ========== 不可变更新方法 ==========

    /**
     * 获取剩余时间（秒）
     */
    public float getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0f, (duration - elapsed) / 1000f);
    }

    /**
     * 检查效果是否已过期
     */
    public boolean isExpired() {
        return getRemainingTime() <= 0f;
    }

    /**
     * 检查是否为永久效果
     */
    public boolean isPermanent() {
        return duration < 0f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CharacterEffectModel)) return false;
        CharacterEffectModel that = (CharacterEffectModel) o;
        return targetCharacterId == that.targetCharacterId &&
               type == that.type &&
               startTime == that.startTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetCharacterId, type, startTime);
    }

    @Override
    public String toString() {
        return "CharacterEffectModel{" +
                "type=" + type +
                ", targetCharacterId=" + targetCharacterId +
                ", progress=" + progress +
                ", isActive=" + isActive +
                '}';
    }
}
