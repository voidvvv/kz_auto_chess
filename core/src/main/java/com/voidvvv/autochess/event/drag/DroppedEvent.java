package com.voidvvv.autochess.event.drag;

/**
 * 拖拽放下事件
 */
public class DroppedEvent implements DragEvent {
    private final Object dropped;
    private final float x, y;
    private final DragTarget targetType;
    private long timestamp;

    public DroppedEvent(Object dropped, float x, float y, DragTarget targetType) {
        this.dropped = dropped;
        this.x = x;
        this.y = y;
        this.targetType = targetType;
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
        return targetType;
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
     * 获取被放下的对象
     * @return 被放下的对象（可能是Card或BattleCharacter）
     */
    public Object getDropped() {
        return dropped;
    }
}
