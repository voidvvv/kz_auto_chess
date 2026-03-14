package com.voidvvv.autochess.updater;

import com.voidvvv.autochess.manage.CollisionManager;
import com.voidvvv.autochess.model.Battlefield;

/**
 * 碰撞更新器
 * 遵循 IUpdater 模式，每帧协调碰撞检测
 *
 * 职责：
 * - 在每帧调用 CollisionManager 执行碰撞检测
 * - 管理 CollisionManager 的生命周期
 */
public class CollisionUpdater implements IUpdater<Battlefield> {

    private final CollisionManager collisionManager;
    private boolean active = true;

    /**
     * 创建碰撞更新器
     *
     * @param collisionManager 碰撞管理器
     */
    public CollisionUpdater(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    @Override
    public void update(Battlefield battlefield, float delta) {
        if (!active || battlefield == null || collisionManager == null) {
            return;
        }

        // 执行所有碰撞检测
        // 1. 更新碰撞上下文缓存
        // 2. 检测角色间碰撞
        // 3. 检测投射物碰撞
        collisionManager.checkAllCollisions(battlefield);
    }

    @Override
    public void initialize(Battlefield battlefield) {
        if (collisionManager != null) {
            collisionManager.clear();
        }
        active = true;
    }

    @Override
    public void dispose() {
        if (collisionManager != null) {
            collisionManager.clear();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void turnOn() {
        this.active = true;
    }

    @Override
    public void turnOff() {
        this.active = false;
    }

    /**
     * 获取碰撞管理器
     *
     * @return CollisionManager 实例
     */
    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
}
