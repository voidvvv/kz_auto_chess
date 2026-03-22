package com.voidvvv.autochess.event;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Logger;

import java.util.List;

/**
 * 游戏事件分发器
 * 负责将事件分发给所有注册的监听器
 */
public class GameEventDispatcher {
    private static final Logger LOGGER = new Logger("GameEventDispatcher", Logger.INFO);

    private final GameEventHolder eventHolder;
    private final GameEventListenerHolder listenerHolder;

    public GameEventDispatcher(GameEventHolder eventHolder, GameEventListenerHolder listenerHolder) {
        this.eventHolder = eventHolder;
        this.listenerHolder = listenerHolder;
    }

    /**
     * 分发所有事件
     */
    public void dispatch() {
        List<GameEventListener> listeners = listenerHolder.getModels();
        if (listeners.isEmpty()) {
            return;
        }

        List<GameEvent> events = eventHolder.getModels();
        if (!events.isEmpty()) {
            LOGGER.info("Dispatching " + events.size() + " events to " + listeners.size() + " listeners");
        }

        for (GameEvent event : events) {
            LOGGER.info("Dispatching event: " + event.getClass().getSimpleName());
            for (GameEventListener listener : listeners) {
                try {
                    listener.onGameEvent(event);
                } catch (Exception e) {
                    LOGGER.error("Error dispatching event " + event.getClass().getSimpleName() + " to listener " + listener.getClass().getSimpleName(), e);
                }
            }
        }
    }
}
