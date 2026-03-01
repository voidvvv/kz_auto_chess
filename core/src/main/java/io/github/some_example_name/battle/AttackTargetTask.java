package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.msg.MessageManager;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.CharacterStats;
import io.github.some_example_name.msg.MessageConstants;

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
        MessageManager.getInstance().dispatchMessage(BattleTelegraph.INSTANCE, bb, MessageConstants.attack, bb);
//
//        float now = bb.getCurrentTime();
//        if (now < self.getNextAttackTime()) {
//            return Task.Status.RUNNING;
//        }
//        DamageEvent de = new DamageEvent();
//        de.setFrom(self);
//        de.setTo(target);
//        float damage = computeDamage(self, target);
//        de.setDamage(new Damage((float)damage, Damage.DamageType.PhySic));
//        bb.getBattlefield().getDamageEventHolder().addModel(de);
//
//        self.setNextAttackTime(now + ATTACK_COOLDOWN);

        return Task.Status.SUCCEEDED;
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
