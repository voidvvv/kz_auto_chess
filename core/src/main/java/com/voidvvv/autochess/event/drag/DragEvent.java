package com.voidvvv.autochess.event.drag;

import com.voidvvv.autochess.event.GameEvent;

/**
 * 拖拽事件接口
 * 所有拖拽相关事件都应实现此接口
 */
public interface DragEvent extends GameEvent {
    /**
     * 拖拽目标类型枚举
     */
    enum DragTarget {
        BATTLEFIELD, DECK, SHOP, CANCEL, NONE
    }

    /**
     * 获取拖拽目标类型
     * @return 拖拽目标类型
     */
    DragTarget getTargetType();

    /**
     * 获取X坐标
     * @return X坐标
     */
    float getX();

    /**
     * 获取Y坐标
     * @return Y坐标
     */
    float getY();
}
