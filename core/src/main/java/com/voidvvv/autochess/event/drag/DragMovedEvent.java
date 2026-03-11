package com.voidvvv.autochess.event.drag;

/**
 * 拖拽移动事件
 */
public class DragMovedEvent implements DragEvent {
    private final float x, y;
    private long timestamp;

    public DragMovedEvent(float x, float y) {
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
        return DragTarget.NONE;  // 移动时目标类型由GameInputHandler内部管理
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }
}
