package com.voidvvv.autochess.event.drag;

/**
 * 拖拽取消事件
 */
public class DragCancelledEvent implements DragEvent {
    private long timestamp;

    public DragCancelledEvent() {
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
        return DragTarget.CANCEL;
    }

    @Override
    public float getX() {
        return 0;
    }

    @Override
    public float getY() {
        return 0;
    }
}
