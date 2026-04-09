package com.voidvvv.autochess.event.combat;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.BattleCharacter;

/**
 * 攻击事件
 * 当角色开始攻击时触发，用于触发攻击动画（抖动）
 */
public class AttackEvent implements GameEvent {
    private long timestamp;
    private final BattleCharacter attacker;
    private final BattleCharacter target;

    /**
     * 构造函数
     *
     * @param attacker 攻击者
     * @param target   目标（可为 null，如果目标已死亡或丢失）
     */
    public AttackEvent(BattleCharacter attacker, BattleCharacter target) {
        this.timestamp = System.currentTimeMillis();
        this.attacker = attacker;
        this.target = target;
    }

    // ========== Getters ==========

    public BattleCharacter getAttacker() {
        return attacker;
    }

    public BattleCharacter getTarget() {
        return target;
    }

    // ========== GameEvent 接口实现 ==========

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AttackEvent{" +
                "attacker=" + (attacker != null ? attacker.getCard().getName() : "null") +
                ", target=" + (target != null ? target.getCard().getName() : "null") +
                '}';
    }
}
