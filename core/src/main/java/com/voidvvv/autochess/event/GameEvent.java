package com.voidvvv.autochess.event;

/**
 * 游戏事件接口
 * 所有游戏事件都需要实现此接口
 */
public interface GameEvent {
    /**
     * 获取事件时间戳
     * @return 事件发生的时间（毫秒）
     */
    long getTimestamp();

    /**
     * 设置事件时间戳
     * @param timestamp 事件发生时间（毫秒）
     */
    void setTimestamp(long timestamp);
}
