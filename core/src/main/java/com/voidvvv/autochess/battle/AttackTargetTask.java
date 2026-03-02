package com.voidvvv.autochess.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.CharacterStats;
import com.voidvvv.autochess.msg.MessageConstants;
import com.voidvvv.autochess.sm.state.common.AttackState;

/**
 * 行为树叶子任务：若黑板目标有效且在攻击范围内，则造成一次伤害并进入冷却
 */
public class AttackTargetTask extends LeafTask<BattleUnitBlackboard> {

    private static final float ATTACK_COOLDOWN = 1f;

    @Override
    public void start() {
    }

    @Override
    public Task.Status execute() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter self = bb.getSelf();
        BattleCharacter target = bb.getTarget();

        if (target == null || target.isDead()) {
            return Task.Status.SUCCEEDED;
        }
        if (self.distanceTo(target) > self.getAttackRange()) {
            return Task.Status.FAILED;
        }
        if (shouldCancel() ) {
            return Status.CANCELLED;
        }
        if (!bb.stateMachine.getCurrent().isState(AttackState.INSTANCE)) {
            MessageManager.getInstance().dispatchMessage(BattleTelegraph.INSTANCE, bb, MessageConstants.attack, bb);

        }
//
//        float now = bb.getCurrentTime();
//        if (now < self.getNextAttackTime()) {
//            return Task.Status.RUNNING;
//        }
//
//        self.setNextAttackTime(now + ATTACK_COOLDOWN);

        return Status.RUNNING;
    }

    private boolean shouldCancel() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter self = bb.getSelf();
        return self.isDead();
    }

    private float computeDamage(BattleCharacter attacker, BattleCharacter defender) {
        CharacterStats as = attacker.getStats();

        return as.getAttack();
    }

    @Override
    protected Task<BattleUnitBlackboard> copyTo(Task<BattleUnitBlackboard> task) {
        return task;
    }
}
