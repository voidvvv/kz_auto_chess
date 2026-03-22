package com.voidvvv.autochess.event;

import com.badlogic.gdx.utils.Logger;
import com.voidvvv.autochess.model.ModelHolder;

/**
 * 游戏事件系统
 * 集中管理事件分发，与现有 ModelHolder 模式保持一致
 */
public class GameEventSystem {

    private static final Logger LOGGER = new Logger("GameEventSystem", Logger.INFO);

    private final GameEventHolder eventHolder;
    private final GameEventListenerHolder listenerHolder;
    private final GameEventDispatcher dispatcher;

    public GameEventSystem() {
        this.eventHolder = new GameEventHolder();
        this.listenerHolder = new GameEventListenerHolder();
        this.dispatcher = new GameEventDispatcher(eventHolder, listenerHolder);
    }

    /**
     * 注册事件监听器
     */
    public void registerListener(GameEventListener listener) {
        LOGGER.info("Registered listener: " + listener.getClass().getSimpleName());
        listenerHolder.addModel(listener);
    }

    /**
     * 注销事件监听器
     */
    public void unregisterListener(GameEventListener listener) {
        listenerHolder.removeModel(listener);
    }

    /**
     * 发布事件
     */
    public void postEvent(GameEvent event) {
        LOGGER.info("Posting event: " + event.getClass().getSimpleName());
        eventHolder.addModel(event);
    }

    /**
     * 分发所有事件
     */
    public void dispatch() {
        dispatcher.dispatch();
    }

    /**
     * 清空事件队列（每帧调用后清空）
     */
    public void clear() {
        eventHolder.clear();
    }
}
