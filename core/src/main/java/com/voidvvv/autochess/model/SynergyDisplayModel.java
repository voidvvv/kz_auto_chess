package com.voidvvv.autochess.model;

import java.util.Objects;

/**
 * 羁绊显示数据模型 - 不可变数据结构
 *
 * 用于羁绊面板渲染，包含羁绊类型、当前数量、激活等级、下一级阈值等信息。
 * 遵循项目 Model-Manager-Render 分离原则中的 Model 层规范。
 *
 * Research Insights:
 * - 遵循 XxxModel 命名规范 (kz-autochess-code-guidelines)
 * - 不可变设计确保线程安全
 * - 直接复用 SynergyManager API，无需额外 DTO 层
 */
public final class SynergyDisplayModel {
    private final SynergyType synergyType;
    private final int currentCount;
    private final int activeLevel;
    private final int nextThreshold;
    private final boolean isActive;
    private final String icon;

    /**
     * 构造羁绊显示模型
     *
     * @param synergyType 羁绊类型（不能为 null）
     * @param currentCount 当前拥有的相同羁绊角色数量（不能为负）
     * @param activeLevel 当前激活等级（0 表示未激活）
     * @param nextThreshold 下一级需要的数量（-1 表示已满级）
     * @param icon 羁绊图标（Unicode emoji 或其他标识）
     * @throws IllegalArgumentException 如果参数无效
     */
    public SynergyDisplayModel(SynergyType synergyType, int currentCount,
                               int activeLevel, int nextThreshold, String icon) {
        // 参数验证（安全审查建议）
        if (synergyType == null) {
            throw new IllegalArgumentException("synergyType cannot be null");
        }
        if (currentCount < 0) {
            throw new IllegalArgumentException("currentCount cannot be negative: " + currentCount);
        }
        if (activeLevel < 0) {
            throw new IllegalArgumentException("activeLevel cannot be negative: " + activeLevel);
        }

        this.synergyType = synergyType;
        this.currentCount = currentCount;
        this.activeLevel = activeLevel;
        this.nextThreshold = nextThreshold;
        this.isActive = activeLevel > 0;
        this.icon = icon != null ? icon : "";
    }

    // ========== 只读 getter 方法 ==========

    public SynergyType getSynergyType() {
        return synergyType;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getActiveLevel() {
        return activeLevel;
    }

    public int getNextThreshold() {
        return nextThreshold;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getIcon() {
        return icon;
    }

    // ========== 便利方法 ==========

    /**
     * 计算下一级进度比例（0.0 ~ 1.0）
     * @return 进度比例，如果已满级返回 1.0
     */
    public float getProgress() {
        if (nextThreshold <= 0) {
            return 1.0f;
        }
        return Math.min(1.0f, (float) currentCount / nextThreshold);
    }

    /**
     * 检查是否即将激活（未激活但已有相同羁绊角色）
     * @return 如果即将激活返回 true
     */
    public boolean isNearActivation() {
        return !isActive && currentCount > 0;
    }

    // ========== Object 方法 ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SynergyDisplayModel that = (SynergyDisplayModel) o;
        return currentCount == that.currentCount &&
               activeLevel == that.activeLevel &&
               isActive == that.isActive &&
               Objects.equals(synergyType, that.synergyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(synergyType, currentCount, activeLevel, isActive);
    }

    @Override
    public String toString() {
        return "SynergyDisplayModel{" +
               "synergyType=" + synergyType +
               ", currentCount=" + currentCount +
               ", activeLevel=" + activeLevel +
               ", nextThreshold=" + nextThreshold +
               ", isActive=" + isActive +
               ", icon='" + icon + '\'' +
               '}';
    }
}
