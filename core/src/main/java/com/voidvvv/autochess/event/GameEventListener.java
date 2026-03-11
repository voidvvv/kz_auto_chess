package com.voidvvv.autochess.event;

/**
 * 游戏事件监听器接口
 * 所有监听游戏事件的组件都应实现此接口
 */
public interface GameEventListener {
    /**
     * 处理游戏事件
     * @param event 游戏事件
     */
    void onGameEvent(GameEvent event);
}
