package com.voidvvv.autochess.sm.state.common;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.msg.MessageConstants;
import com.voidvvv.autochess.sm.state.AbstractState;
import com.voidvvv.autochess.sm.state.BaseState;

public class AttackState extends AbstractState {
    public static final AttackState INSTANCE = new AttackState();

    @Override
    public String name() {
        return "attack";
    }

    @Override
    public boolean canExit(BattleUnitBlackboard entity, BaseState<BattleUnitBlackboard> nextState) {
        if (nextState instanceof MoveState) {
            return entity.getSelf().canMoveWhileAttacking();
        }
        return true;
    }

    @Override
    protected void onEnter(BattleUnitBlackboard entity) {
        entity.couldDamage = true;
    }

    @Override
    protected void onUpdate(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().currentAttackProgress += delta;

        float progress = entity.getSelf().currentAttackProgress;

        if (progress >= entity.getSelf().maxAttackActProgress) {
            // 攻击动作结束 — 如果还没造成伤害则补发 doAttack
            if (entity.couldDamage) {
                MessageManager.getInstance().dispatchMessage(
                    entity.stateMachine, entity, MessageConstants.doAttack, "");
                entity.getSelf().attackCooldown = 1f;
                entity.couldDamage = false;
            }
            MessageManager.getInstance().dispatchMessage(
                entity.stateMachine, entity, MessageConstants.endAttack, "");
        } else if (progress >= entity.getSelf().progressCouldDamage && entity.couldDamage) {
            // 到达伤害判定点
            MessageManager.getInstance().dispatchMessage(
                entity.stateMachine, entity, MessageConstants.doAttack, "");
            entity.getSelf().attackCooldown = 1f;
            entity.couldDamage = false;
        }
    }

    @Override
    protected void onExit(BattleUnitBlackboard entity) {
        entity.getSelf().currentAttackProgress = 0f;
    }
}
