package com.voidvvv.autochess.event;

/**
 * 玩家死亡事件
 * 当玩家血量归零时触发
 */
public class PlayerDeathEvent implements GameEvent {
    private final int finalRemainingHealth;
    private final int maxReachedLevel;
    private final long timestamp;

    public PlayerDeathEvent(int finalRemainingHealth, int maxReachedLevel) {
        this.finalRemainingHealth = finalRemainingHealth;
        this.maxReachedLevel = maxReachedLevel;
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

    public int getFinalRemainingHealth() {
        return finalRemainingHealth;
    }

    public int getMaxReachedLevel() {
        return maxReachedLevel;
    }
}
