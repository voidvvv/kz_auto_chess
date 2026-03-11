package com.voidvvv.autochess.event;

import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.event.GameEvent;

/**
 * 游戏事件监听器容器
 * 使用 ModelHolder 模式复用现有设计
 */
public class GameEventListenerHolder extends ModelHolder<GameEventListener> {
    // 空，只是类型安全的容器
}
