package com.voidvvv.autochess.updater;

import com.voidvvv.autochess.model.Projectile;
import com.badlogic.gdx.math.Vector2;

/**
 * 投掷物更新器
 * 负责更新投掷物的位置和状态，实现与数据模型的分离
 */
public class ProjectileUpdater {

    /**
     * 更新投掷物状态
     * @param projectile 要更新的投掷物
     * @param deltaTime 时间增量（秒）
     * @return true 如果投掷物应该被移除（超出范围或已命中）
     */
    public boolean update(Projectile projectile, float deltaTime) {
        if (projectile.hasHit()) {
            return true; // 已命中，需要移除
        }

        // 根据类型更新飞行方向
        switch (projectile.getType()) {
            case ARROW:
                // 直线飞行，方向保持不变
                break;
            case MAGIC_BALL:
                // 追踪目标
                if (projectile.getTarget() != null && !projectile.getTarget().isDead()) {
                    updateTrackingDirection(projectile, deltaTime);
                }
                break;
        }

        // 获取投掷物方向向量
        Vector2 direction = projectile.getDirection();

        // 移动投掷物
        float moveX = direction.x * projectile.getSpeed() * deltaTime;
        float moveY = direction.y * projectile.getSpeed() * deltaTime;
        // 注意：这里无法直接更新projectile的x,y，需要添加setter方法或通过其他方式

        // 计算新的位置（这里需要Projectile类有setX和setY方法）
        projectile.setX(projectile.getX() + moveX);
        projectile.setY(projectile.getY() + moveY);

        // 更新飞行距离
        projectile.setDistanceTraveled(
            projectile.getDistanceTraveled() + (float) Math.sqrt(moveX * moveX + moveY * moveY)
        );

        // 更新粒子生成计时器
        projectile.setParticleSpawnTimer(projectile.getParticleSpawnTimer() + deltaTime);

        // 检查是否超出最大飞行距离
        if (projectile.getDistanceTraveled() >= projectile.getMaxFlightDistance()) {
            return true; // 超出范围，需要移除
        }

        return false; // 继续飞行
    }

    /**
     * 更新方向指向目标（用于初始化或重新计算方向）
     */
    public void updateDirectionToTarget(Projectile projectile) {
        if (projectile.getTarget() == null) return;

        float x = projectile.getX();
        float y = projectile.getY();
        float dx = projectile.getTarget().getX() - x;
        float dy = projectile.getTarget().getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.001f) {
            Vector2 direction = projectile.getDirection();
            direction.x = dx / distance;
            direction.y = dy / distance;
        }
    }

    /**
     * 更新追踪方向（平滑转向）
     */
    private void updateTrackingDirection(Projectile projectile, float deltaTime) {
        if (projectile.getTarget() == null || projectile.getTarget().isDead()) return;

        // 计算目标方向
        float x = projectile.getX();
        float y = projectile.getY();
        float dx = projectile.getTarget().getX() - x;
        float dy = projectile.getTarget().getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.001f) return;

        Vector2 direction = projectile.getDirection();

        // 计算当前方向与目标方向之间的角度差
        float targetAngle = (float) Math.atan2(dy, dx);
        float currentAngle = (float) Math.atan2(direction.y, direction.x);

        // 计算角度差（规范化到 -PI 到 PI）
        float angleDiff = targetAngle - currentAngle;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

        // 限制转向速度
        float maxAngleChange = projectile.getCurrentTrackingSpeed() * (float) Math.PI / 180f * deltaTime;
        if (Math.abs(angleDiff) > maxAngleChange) {
            angleDiff = Math.signum(angleDiff) * maxAngleChange;
        }

        // 应用角度变化
        float newAngle = currentAngle + angleDiff;
        direction.x = (float) Math.cos(newAngle);
        direction.y = (float) Math.sin(newAngle);
    }

    /**
     * 检查是否可以生成粒子
     */
    public boolean shouldSpawnParticle(Projectile projectile) {
        if (projectile.getParticleSpawnTimer() >= projectile.getParticleSpawnInterval()) {
            projectile.setParticleSpawnTimer(0f);
            return true;
        }
        return false;
    }
}