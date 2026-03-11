package com.voidvvv.autochess.event.drag;

/**
 * 拖拽开始事件
 */
public class DragStartedEvent implements DragEvent {
    private final Object dragged;
    private final float x, y;
    private long timestamp;

    public DragStartedEvent(Object dragged, float x, float y) {
        this.dragged = dragged;
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public DragTarget getTargetType() {
        return DragTarget.NONE;  // 拖拽开始时目标类型为NONE
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    /**
     * 获取被拖拽的对象
     * @return 被拖拽的对象（可能是Card或BattleCharacter）
     */
    public Object getDragged() {
        return dragged;
    }
}
