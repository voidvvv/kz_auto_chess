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
            initDirectionToTarget();
        }
    }

    
    /**
     * 初始化方向指向目标（私有方法，仅用于构造器）
     */
    private void initDirectionToTarget() {
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
     * 命中目标
     */
    public void hit() {
        hasHit = true;
    }

    // Setter 方法
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setDistanceTraveled(float distanceTraveled) { this.distanceTraveled = distanceTraveled; }
    public void setParticleSpawnTimer(float particleSpawnTimer) { this.particleSpawnTimer = particleSpawnTimer; }
    public float getParticleSpawnTimer() { return particleSpawnTimer; }
    public float getParticleSpawnInterval() { return particleSpawnInterval; }
    public float getCurrentTrackingSpeed() { return currentTrackingSpeed; }

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
}