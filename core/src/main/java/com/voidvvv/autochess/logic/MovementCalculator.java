package com.voidvvv.autochess.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.voidvvv.autochess.model.MoveComponent;
import com.voidvvv.autochess.model.MovementEffect;
import com.voidvvv.autochess.model.MovementEffectType;

/**
 * 移动计算器
 * 负责计算角色移动向量
 * 从模型中分离计算逻辑，使模型保持纯数据容器
 */
public class MovementCalculator {

    private final Vector2 tmp = new Vector2();

    // 速度上限（防止无限加速）
    private static final float MAX_VELOCITY = 500f;

    /**
     * 计算总移动向量（增强版，支持移动效果）
     * @param moveComponent 移动组件
     * @return 总移动向量
     */
    public Vector2 calculateTotalMove(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return new Vector2(0, 0);
        }

        tmp.set(0, 0);

        // 1. 检查禁锢效果
        if (isImmobilized(moveComponent)) {
            // 禁锢时只计算外部拖拽速度，不计算自身移动
            tmp.add(calculateTotalDragVelocity(moveComponent));
        } else {
            // 2. 检查强制速度效果
            MovementEffect fixedVel = getActiveFixedVelocityEffect(moveComponent);

            if (fixedVel != null) {
                // 强制速度覆盖自身移动
                tmp.set(fixedVel.getVelocity());
            } else {
                // 3. 计算自身移动（应用速度修正）
                if (moveComponent.canWalk) {
                    float modifiedSpeed = moveComponent.speed * calculateSpeedModifier(moveComponent);
                    tmp.set(moveComponent.dir).nor().scl(modifiedSpeed);
                }

                // 4. 叠加外部拖拽速度
                tmp.add(calculateTotalDragVelocity(moveComponent));
            }
        }

        // 5. 应用速度上限
        if (tmp.len() > MAX_VELOCITY) {
            tmp.nor().scl(MAX_VELOCITY);
        }

        return tmp.cpy();
    }

    /**
     * 检查是否被禁锢
     * @param moveComponent 移动组件
     * @return 如果被禁锢返回true
     */
    public boolean isImmobilized(MoveComponent moveComponent) {
        if (moveComponent == null) return false;
        Array<MovementEffect> effects = moveComponent.movementEffects;
        if (effects == null) return false;
        for (MovementEffect effect : effects) {
            if (effect.getType() == MovementEffectType.IMMOBILIZE && !effect.isExpired()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前激活的强制速度效果（如果有）
     * @param moveComponent 移动组件
     * @return 最高优先级的强制速度效果，如果没有返回null
     */
    public MovementEffect getActiveFixedVelocityEffect(MoveComponent moveComponent) {
        if (moveComponent == null) return null;
        Array<MovementEffect> effects = moveComponent.movementEffects;
        if (effects == null) return null;
        MovementEffect highest = null;
        for (MovementEffect effect : effects) {
            if (effect.getType() == MovementEffectType.FIXED_VELOCITY && !effect.isExpired()) {
                if (highest == null || effect.getPriority() > highest.getPriority()) {
                    highest = effect;
                }
            }
        }
        return highest;
    }

    /**
     * 计算总拖拽速度（DRAG效果叠加）
     * @param moveComponent 移动组件
     * @return 拖拽速度向量
     */
    public Vector2 calculateTotalDragVelocity(MoveComponent moveComponent) {
        Vector2 total = new Vector2(0, 0);
        if (moveComponent == null) return total;
        Array<MovementEffect> effects = moveComponent.movementEffects;
        if (effects == null) return total;

        for (MovementEffect effect : effects) {
            if (effect.getType() == MovementEffectType.DRAG && !effect.isExpired()) {
                total.add(effect.getVelocity());
            }
        }
        return total;
    }

    /**
     * 计算速度修正系数（乘法叠加）
     * @param moveComponent 移动组件
     * @return 速度修正系数（1.0=无变化）
     */
    public float calculateSpeedModifier(MoveComponent moveComponent) {
        if (moveComponent == null) return 1.0f;
        Array<MovementEffect> effects = moveComponent.movementEffects;
        if (effects == null) return 1.0f;

        float modifier = 1.0f;
        for (MovementEffect effect : effects) {
            if (effect.getType() == MovementEffectType.SPEED_MODIFIER && !effect.isExpired()) {
                modifier *= effect.getSpeedModifier();
            }
        }
        return modifier;
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
     * 获取移动速度（应用速度修正后）
     * @param moveComponent 移动组件
     * @return 移动速度（标量）
     */
    public float getMoveSpeed(MoveComponent moveComponent) {
        if (moveComponent == null || !moveComponent.canWalk) {
            return 0;
        }
        return moveComponent.speed * calculateSpeedModifier(moveComponent);
    }
}
