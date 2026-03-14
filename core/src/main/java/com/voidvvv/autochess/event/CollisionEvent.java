package com.voidvvv.autochess.event;

/**
 * 碰撞事件
 * 当碰撞发生时通过事件系统通知相关系统
 */
public class CollisionEvent implements GameEvent {

    /**
     * 碰撞类型枚举
     */
    public enum CollisionType {
        /** 角色与角色碰撞 */
        CHARACTER_CHARACTER,
        /** 角色与投射物碰撞 */
        CHARACTER_PROJECTILE,
        /** 投射物与投射物碰撞 */
        PROJECTILE_PROJECTILE
    }

    private long timestamp;
    private CollisionType collisionType;
    private Object source;      // 碰撞发起方（BattleCharacter 或 Projectile）
    private Object target;      // 碰撞目标（BattleCharacter 或 Projectile）
    private String collisionArea; // 碰撞区域："face" 或 "body"（仅对角色间碰撞有意义）

    public CollisionEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public CollisionEvent(CollisionType collisionType, Object source, Object target) {
        this.timestamp = System.currentTimeMillis();
        this.collisionType = collisionType;
        this.source = source;
        this.target = target;
    }

    // ==================== GameEvent 接口实现 ====================

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ==================== Getters & Setters ====================

    public CollisionType getCollisionType() {
        return collisionType;
    }

    public void setCollisionType(CollisionType collisionType) {
        this.collisionType = collisionType;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public String getCollisionArea() {
        return collisionArea;
    }

    public void setCollisionArea(String collisionArea) {
        this.collisionArea = collisionArea;
    }

    // ==================== 便捷方法 ====================

    /**
     * 检查是否为角色间碰撞
     */
    public boolean isCharacterCollision() {
        return collisionType == CollisionType.CHARACTER_CHARACTER;
    }

    /**
     * 检查是否为角色与投射物碰撞
     */
    public boolean isCharacterProjectileCollision() {
        return collisionType == CollisionType.CHARACTER_PROJECTILE;
    }

    /**
     * 检查是否为投射物间碰撞
     */
    public boolean isProjectileCollision() {
        return collisionType == CollisionType.PROJECTILE_PROJECTILE;
    }

    /**
     * 检查是否为 face 区域碰撞
     */
    public boolean isFaceCollision() {
        return "face".equals(collisionArea);
    }

    /**
     * 检查是否为 body 区域碰撞
     */
    public boolean isBodyCollision() {
        return "body".equals(collisionArea);
    }

    @Override
    public String toString() {
        return "CollisionEvent{" +
                "type=" + collisionType +
                ", source=" + source +
                ", target=" + target +
                ", area='" + collisionArea + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
