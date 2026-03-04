package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;

/**
 * 投掷物类
 * 表示战场上飞行的攻击物（箭矢、魔法球等）
 */
public class Projectile {

    public enum ProjectileType {
        ARROW,      // 弓箭手的箭矢，直线飞行
        MAGIC_BALL  // 法师的魔法球，追踪目标
    }

    // 位置和速度属性
    private float x, y;
    private float radius = 8f; // 投掷物半径
    private float speed = 300f; // 飞行速度（像素/秒）
    private Vector2 direction = new Vector2(); // 飞行方向（单位向量）
    private ProjectileType type;

    // 来源和目标
    private BattleCharacter source;
    private BattleCharacter target;

    // 伤害属性
    private float damage;
    private boolean hasHit = false;

    // 飞行限制
    private float maxFlightDistance = 800f; // 最大飞行距离
    private float distanceTraveled = 0f;

    // 追踪属性（仅对追踪型投掷物有效）
    private float trackingSpeed = 180f; // 转向速度（角度/秒）
    private float currentTrackingSpeed = 180f;

    // 视觉效果属性
    private float particleSpawnTimer = 0f;
    private float particleSpawnInterval = 0.02f; // 粒子生成间隔（秒）

    /**
     * 创建投掷物
     */
    public Projectile(float startX, float startY, BattleCharacter source, BattleCharacter target,
                     float damage, ProjectileType type) {
        this.x = startX;
        this.y = startY;
        this.source = source;
        this.target = target;
        this.damage = damage;
        this.type = type;

        // 根据类型设置不同属性
        switch (type) {
            case ARROW:
                this.radius = 6f;
                this.speed = 400f;
                this.maxFlightDistance = 700f;
                this.particleSpawnInterval = 0.03f;
                break;
            case MAGIC_BALL:
                this.radius = 12f;
                this.speed = 250f;
                this.maxFlightDistance = 600f;
                this.trackingSpeed = 200f;
                this.currentTrackingSpeed = trackingSpeed;
                this.particleSpawnInterval = 0.015f;
                break;
        }

        // 初始化飞行方向
        if (target != null) {
            updateDirectionToTarget();
        }
    }

    /**
     * 更新投掷物状态
     * @param deltaTime 时间增量（秒）
     * @return true 如果投掷物应该被移除（超出范围或已命中）
     */
    public boolean update(float deltaTime) {
        if (hasHit) {
            return true; // 已命中，需要移除
        }

        // 根据类型更新飞行方向
        switch (type) {
            case ARROW:
                // 直线飞行，方向保持不变
                break;
            case MAGIC_BALL:
                // 追踪目标
                if (target != null && !target.isDead()) {
                    updateTrackingDirection(deltaTime);
                }
                break;
        }

        // 移动投掷物
        float moveX = direction.x * speed * deltaTime;
        float moveY = direction.y * speed * deltaTime;
        x += moveX;
        y += moveY;

        // 更新飞行距离
        distanceTraveled += Math.sqrt(moveX * moveX + moveY * moveY);

        // 更新粒子生成计时器
        particleSpawnTimer += deltaTime;

        // 检查是否超出最大飞行距离
        if (distanceTraveled >= maxFlightDistance) {
            return true; // 超出范围，需要移除
        }

        return false; // 继续飞行
    }

    /**
     * 更新方向指向目标
     */
    private void updateDirectionToTarget() {
        if (target == null) return;

        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.001f) {
            direction.x = dx / distance;
            direction.y = dy / distance;
        }
    }

    /**
     * 更新追踪方向（平滑转向）
     */
    private void updateTrackingDirection(float deltaTime) {
        if (target == null || target.isDead()) return;

        // 计算目标方向
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.001f) return;

        // 计算当前方向与目标方向之间的角度差
        float targetAngle = (float) Math.atan2(dy, dx);
        float currentAngle = (float) Math.atan2(direction.y, direction.x);

        // 计算角度差（规范化到 -PI 到 PI）
        float angleDiff = targetAngle - currentAngle;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        // 限制转向速度
        float maxAngleChange = currentTrackingSpeed * (float) Math.PI / 180f * deltaTime;
        if (Math.abs(angleDiff) > maxAngleChange) {
            angleDiff = Math.signum(angleDiff) * maxAngleChange;
        }

        // 应用角度变化
        float newAngle = currentAngle + angleDiff;
        direction.x = (float) Math.cos(newAngle);
        direction.y = (float) Math.sin(newAngle);
    }

    /**
     * 命中目标
     */
    public void hit() {
        hasHit = true;
    }

    /**
     * 检查是否可以生成粒子
     */
    public boolean shouldSpawnParticle() {
        if (particleSpawnTimer >= particleSpawnInterval) {
            particleSpawnTimer = 0f;
            return true;
        }
        return false;
    }

    // Getter 方法
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public float getCollisionRadius() { return radius; }
    public ProjectileType getType() { return type; }
    public BattleCharacter getSource() { return source; }
    public BattleCharacter getTarget() { return target; }
    public float getDamage() { return damage; }
    public boolean hasHit() { return hasHit; }
    public Vector2 getDirection() { return direction; }
    public float getSpeed() { return speed; }
    public float getDistanceTraveled() { return distanceTraveled; }
    public float getMaxFlightDistance() { return maxFlightDistance; }

    /**
     * 获取投掷物颜色（用于渲染）
     */
    public com.badlogic.gdx.graphics.Color getColor() {
        switch (type) {
            case ARROW:
                return com.badlogic.gdx.graphics.Color.BROWN;
            case MAGIC_BALL:
                return com.badlogic.gdx.graphics.Color.PURPLE;
            default:
                return com.badlogic.gdx.graphics.Color.WHITE;
        }
    }
}