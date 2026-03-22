package com.voidvvv.autochess.manage;

import com.voidvvv.autochess.model.ActiveEffect;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.EventEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 活跃效果管理器
 * 管理从事件获得的持久化效果
 */
public class ActiveEffectsManager {

    private final List<ActiveEffect> activeEffects;

    public ActiveEffectsManager() {
        this.activeEffects = new ArrayList<>();
    }

    /**
     * 添加效果
     */
    public void addEffect(ActiveEffect effect) {
        activeEffects.add(effect);
    }

    /**
     * 移除效果
     */
    public void removeEffect(ActiveEffect effect) {
        activeEffects.remove(effect);
    }

    /**
     * 清除所有效果
     */
    public void clear() {
        activeEffects.clear();
    }

    /**
     * 获取所有活跃效果
     */
    public List<ActiveEffect> getActiveEffects() {
        return new ArrayList<>(activeEffects);
    }

    /**
     * 应用效果到角色（战斗开始前调用）
     */
    public void applyToCharacter(BattleCharacter character) {
        for (ActiveEffect effect : activeEffects) {
            switch (effect.getEffectType()) {
                case ATTACK_BOOST:
                    // TODO: 应用攻击力加成
                    break;
                case HEALTH_BOOST:
                    // TODO: 应用生命值加成
                    break;
                case DEFENSE_BOOST:
                    // TODO: 应用防御力加成
                    break;
                case HEAL:
                    // 回复生命（暂时不做处理，因为需要 BattleCharacter 的具体 API）
                    break;
                case GOLD:
                case FREE_CARD:
                    // 这些效果在获得时已经应用，不需要应用到角色
                    break;
            }
        }
    }

    /**
     * 获取攻击力加成比例
     */
    public float getAttackBoost() {
        float total = 0;
        for (ActiveEffect effect : activeEffects) {
            if (effect.getEffectType() == EventEffectType.ATTACK_BOOST) {
                total += effect.getValue();
            }
        }
        return total;
    }

    /**
     * 获取生命值加成比例
     */
    public float getHealthBoost() {
        float total = 0;
        for (ActiveEffect effect : activeEffects) {
            if (effect.getEffectType() == EventEffectType.HEALTH_BOOST) {
                total += effect.getValue();
            }
        }
        return total;
    }

    /**
     * 获取防御力加成比例
     */
    public float getDefenseBoost() {
        float total = 0;
        for (ActiveEffect effect : activeEffects) {
            if (effect.getEffectType() == EventEffectType.DEFENSE_BOOST) {
                total += effect.getValue();
            }
        }
        return total;
    }

    /**
     * 检查是否有活跃效果
     */
    public boolean hasActiveEffects() {
        return !activeEffects.isEmpty();
    }
}
