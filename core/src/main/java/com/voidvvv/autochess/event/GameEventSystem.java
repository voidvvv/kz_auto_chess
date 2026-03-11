package com.voidvvv.autochess.event;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Logger;
import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventDispatcher;

/**
 * 游戏事件系统
 * 集中管理事件分发，与现有 ModelHolder 模式保持一致
 */
public class GameEventSystem {

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
