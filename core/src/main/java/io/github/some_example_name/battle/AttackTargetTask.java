package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.CharacterStats;
import io.github.some_example_name.model.battle.Damage;
import io.github.some_example_name.event.DamageEvent;

/**
 * 行为树叶子任务：若黑板目标有效且在攻击范围内，则造成一次伤害并进入冷却
 */
public class AttackTargetTask extends LeafTask<BattleUnitBlackboard> {

    private static final float ATTACK_COOLDOWN = 1f;

    @Override
    public Task.Status execute() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter self = bb.getSelf();
        BattleCharacter target = bb.getTarget();

        if (target == null || target.isDead()) {
            return Task.Status.FAILED;
        }
        if (self.distanceTo(target) > self.getAttackRange()) {
            return Task.Status.FAILED;
        }

        float now = bb.getCurrentTime();
        if (now < self.getNextAttackTime()) {
            return Task.Status.RUNNING;
        }
        DamageEvent de = new DamageEvent();
        de.setFrom(self);
        de.setTo(target);
        float damage = computeDamage(self, target);
        de.setDamage(new Damage((float)damage, Damage.DamageType.PhySic));
        bb.getBattlefield().getDamageEventHolder().addModel(de);

        self.setNextAttackTime(now + ATTACK_COOLDOWN);
        float newHp = Math.max(0f, target.getCurrentHp() - damage);
        target.setCurrentHp(newHp);

        return Task.Status.SUCCEEDED;
    }

    private float computeDamage(BattleCharacter attacker, BattleCharacter defender) {
        CharacterStats as = attacker.getStats();
        CharacterStats ds = defender.getStats();
        if (as == null || ds == null) return 10;
        int raw = as.getAttack();
        int def = ds.getDefense();
        float d = Math.max(1, raw - def / 2);
        return d;
    }

    @Override
    protected Task<BattleUnitBlackboard> copyTo(Task<BattleUnitBlackboard> task) {
        return task;
    }
}
