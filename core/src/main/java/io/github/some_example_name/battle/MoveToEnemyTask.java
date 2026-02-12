package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.model.BattleCharacter;

public class MoveToEnemyTask extends LeafTask<BattleUnitBlackboard> {
    @Override
    public Status execute() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter target = bb.getTarget();
        BattleCharacter self = bb.getSelf();
        if (target == null || target.isDead() || self == null || self.isDead()) {
            return Status.FAILED;
        }
        float targetX = target.getX();
        float targetY = target.getY();
        float x = self.getX();
        float y = self.getY();

        float attackRange = self.getAttackRange();
        float dst = Vector2.dst(x, y, targetX, targetY);
        if (dst > attackRange) {
            self.moveComponent.dir.set(targetX - x, targetY - y);
            return Status.RUNNING;
        } else {
            return Status.SUCCEEDED;
        }
    }

    @Override
    protected Task<BattleUnitBlackboard> copyTo(Task<BattleUnitBlackboard> task) {
        return task;
    }
}
