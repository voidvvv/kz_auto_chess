package com.voidvvv.autochess.event;

import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.event.GameEvent;

/**
 * 游戏事件容器
 * 使用 ModelHolder 模式复用现有设计
 */
public class GameEventHolder extends ModelHolder<GameEvent> {
    // 空，只是类型安全的容器
}
