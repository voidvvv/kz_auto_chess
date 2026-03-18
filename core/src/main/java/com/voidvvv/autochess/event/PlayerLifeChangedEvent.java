package com.voidvvv.autochess.event;

/**
 * 玩家血量变化事件
 * 当玩家血量发生变化时触发
 */
public class PlayerLifeChangedEvent implements GameEvent {
    private final int previousHealth;
    private final int newHealth;
    private final int damageTaken;
    private final boolean isDead;
    private final long timestamp;

    public PlayerLifeChangedEvent(int previousHealth, int newHealth, int damageTaken, boolean isDead) {
        this.previousHealth = previousHealth;
        this.newHealth = newHealth;
        this.damageTaken = damageTaken;
        this.isDead = isDead;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        // 时间戳在构造时设置，不支持修改
    }

    public int getPreviousHealth() {
        return previousHealth;
    }

    public int getNewHealth() {
        return newHealth;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public boolean isDead() {
        return isDead;
    }
}
