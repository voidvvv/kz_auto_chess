package com.voidvvv.autochess.model;

/**
 * 玩家血量数据模型
 *
 * 使用不可变设计模式，所有修改操作返回新的实例
 * 这样可以避免隐藏的副作用，使调试更容易
 */
public final class PlayerLifeModel {
    private final int currentHealth;
    private final int maxHealth;
    private final boolean isDead;

    public PlayerLifeModel(int initialHealth) {
        this.currentHealth = initialHealth;
        this.maxHealth = initialHealth;
        this.isDead = false;
    }

    private PlayerLifeModel(int currentHealth, int maxHealth, boolean isDead) {
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.isDead = isDead;
    }

    /**
     * 承受伤害，返回新的状态
     * @param damage 伤害值
     * @return 新的血量模型实例
     */
    public PlayerLifeModel takeDamage(int damage) {
        if (damage <= 0 || isDead) {
            return this;
        }
        int newHealth = Math.max(0, currentHealth - damage);
        boolean newDead = newHealth <= 0;
        return new PlayerLifeModel(newHealth, maxHealth, newDead);
    }

    /**
     * 恢复血量，返回新的状态
     * @param healAmount 恢复量
     * @return 新的血量模型实例
     */
    public PlayerLifeModel heal(int healAmount) {
        if (healAmount <= 0 || isDead) {
            return this;
        }
        int newHealth = Math.min(maxHealth, currentHealth + healAmount);
        return new PlayerLifeModel(newHealth, maxHealth, isDead);
    }

    /**
     * 重置到初始状态（用于新游戏开始）
     * @param initialHealth 初始血量
     * @return 新的血量模型实例
     */
    public PlayerLifeModel reset(int initialHealth) {
        return new PlayerLifeModel(initialHealth);
    }

    // Getters
    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getMissingHealth() {
        return maxHealth - currentHealth;
    }

    /**
     * 获取血量百分比 (0.0 - 1.0)
     */
    public float getHealthPercentage() {
        if (maxHealth == 0) return 0f;
        return (float) currentHealth / maxHealth;
    }

    @Override
    public String toString() {
        return String.format("PlayerLifeModel{currentHealth=%d, maxHealth=%d, isDead=%s}",
                currentHealth, maxHealth, isDead);
    }
}
