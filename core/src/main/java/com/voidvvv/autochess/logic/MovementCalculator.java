package com.voidvvv.autochess.logic;

import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.model.MoveComponent;

/**
 * 移动计算器
 * 负责计算角色移动向量
 * 从模型中分离计算逻辑，使模型保持纯数据容器
 */
public class MovementCalculator {

    private final Vector2 tmp = new Vector2();

    /**
     * 计算总移动向量
     * @param moveComponent 移动组件
     * @return 总移动向量
     */
    public Vector2 calculateTotalMove(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return new Vector2(0, 0);
        }

        if (moveComponent.canWalk) {
            tmp.set(moveComponent.dir).nor().scl(moveComponent.speed);
        } else {
            tmp.set(0, 0);
        }
        return tmp.add(moveComponent.otherVel);
    }

    /**
     * 检查角色是否正在移动
     * @param moveComponent 移动组件
     * @return 如果角色有移动则返回true
     */
    public boolean isMoving(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return false;
        }
        return moveComponent.canWalk && moveComponent.speed > 0;
    }

    /**
     * 获取移动速度
     * @param moveComponent 移动组件
     * @return 移动速度（标量）
     */
    public float getMoveSpeed(MoveComponent moveComponent) {
        if (moveComponent == null || !moveComponent.canWalk) {
            return 0;
        }
        return moveComponent.speed;
    }
}
