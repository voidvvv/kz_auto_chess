package com.voidvvv.autochess.sm.state.common;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.msg.MessageConstants;
import com.voidvvv.autochess.sm.state.BaseState;

public class AttackState implements BaseState<BattleUnitBlackboard> {
    public static final AttackState INSTANCE = new AttackState();
    @Override
    public void enter(BattleUnitBlackboard entity) {
        entity.getSelf().currentTime = 0f;
        entity.couldDamage = true;
    }

    @Override
    public boolean isState(BaseState<BattleUnitBlackboard> other) {
        return other == this;
    }

    @Override
    public String name() {
        return "attack";
    }

    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time+= delta;
        entity.getSelf().currentTime+= delta;

        entity.getSelf().currentAttackProgress += delta;
        float maxAttackProgress = entity.getSelf().progressCouldDamage;

        if (entity.getSelf().currentAttackProgress>= maxAttackProgress) {
            // attack
            MessageManager.getInstance().dispatchMessage(entity.stateMachine, entity, MessageConstants.doAttack, "");

            entity.couldDamage = false;
        }
        if (entity.getSelf().currentAttackProgress >= entity.getSelf().maxAttackActProgress) {
            MessageManager.getInstance().dispatchMessage(entity.stateMachine, entity, MessageConstants.endAttack, "");

        }
    }

    @Override
    public void exit(BattleUnitBlackboard entity) {
        entity.getSelf().currentAttackProgress = 0f;
        entity.getSelf().lastStateTime = entity.getSelf().currentTime;
    }

    @Override
    public boolean onMessage(BattleUnitBlackboard entity, Telegram telegram) {
        return false;
    }
}
