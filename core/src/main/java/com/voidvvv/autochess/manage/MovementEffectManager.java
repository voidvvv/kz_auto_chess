package com.voidvvv.autochess.manage;

import com.badlogic.gdx.utils.Array;
import com.voidvvv.autochess.model.MoveComponent;
import com.voidvvv.autochess.model.MovementEffect;
import com.voidvvv.autochess.model.MovementEffectType;

/**
 * 移动效果管理器 - 只负责效果的生命周期管理
 * 计算逻辑在 MovementCalculator 中
 */
public class MovementEffectManager {

    /**
     * 添加效果到组件
     * @param component 移动组件
     * @param effect 要添加的效果
     */
    public void addEffect(MoveComponent component, MovementEffect effect) {
        if (effect == null || component == null) return;

        // 检查是否需要替换同ID效果
        removeEffect(component, effect.getEffectId());

        // 根据类型处理叠加
        switch (effect.getType()) {
            case IMMOBILIZE:
                // 禁锢效果：如果已存在禁锢，取较长持续时间
                MovementEffect existing = findEffectByType(component, MovementEffectType.IMMOBILIZE);
                if (existing != null && existing.getRemainingDuration() < effect.getDuration()) {
                    removeEffect(component, existing.getEffectId());
                    component.movementEffects.add(effect);
                } else if (existing == null) {
                    component.movementEffects.add(effect);
                }
                break;

            case FIXED_VELOCITY:
                // 强制速度：移除低优先级同类型效果
                removeLowerPriorityEffects(component, MovementEffectType.FIXED_VELOCITY, effect.getPriority());
                component.movementEffects.add(effect);
                break;

            default:
                // DRAG 和 SPEED_MODIFIER：直接叠加
                component.movementEffects.add(effect);
                break;
        }
    }

    /**
     * 移除效果
     * @param component 移动组件
     * @param effectId 效果ID
     */
    public void removeEffect(MoveComponent component, String effectId) {
        if (component == null || effectId == null) return;
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            if (effectId.equals(effects.get(i).getEffectId())) {
                effects.removeIndex(i);
                break;
            }
        }
    }

    /**
     * 按来源移除所有效果
     * @param component 移动组件
     * @param sourceId 来源ID
     */
    public void removeEffectsBySource(MoveComponent component, String sourceId) {
        if (component == null || sourceId == null) return;
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            if (sourceId.equals(effects.get(i).getSourceId())) {
                effects.removeIndex(i);
            }
        }
    }

    /**
     * 更新所有效果（每帧调用）
     * @param component 移动组件
     * @param delta 时间增量
     */
    public void updateEffects(MoveComponent component, float delta) {
        if (component == null) return;
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            MovementEffect effect = effects.get(i);

            // 更新持续时间
            if (!effect.isPermanent()) {
                effect.setRemainingDuration(effect.getRemainingDuration() - delta);
            }

            // 应用衰减
            if (effect.getDecay() > 0) {
                float decayFactor = 1.0f - effect.getDecay() * delta;
                effect.getVelocity().scl(decayFactor);
            }

            // 移除过期效果
            if (effect.isExpired()) {
                effects.removeIndex(i);
            }
        }
    }

    /**
     * 清除所有效果
     * @param component 移动组件
     */
    public void clearEffects(MoveComponent component) {
        if (component == null) return;
        component.movementEffects.clear();
    }

    // 私有辅助方法
    private MovementEffect findEffectByType(MoveComponent component, MovementEffectType type) {
        for (MovementEffect effect : component.movementEffects) {
            if (effect.getType() == type && !effect.isExpired()) {
                return effect;
            }
        }
        return null;
    }

    private void removeLowerPriorityEffects(MoveComponent component, MovementEffectType type, int minPriority) {
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            MovementEffect effect = effects.get(i);
            if (effect.getType() == type && effect.getPriority() < minPriority) {
                effects.removeIndex(i);
            }
        }
    }
}
