package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.sm.state.common.States;

public class MoveToEnemyTask extends LeafTask<BattleUnitBlackboard> {
    boolean firstFrame = false;
    @Override
    public void start() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter target = bb.getTarget();
        BattleCharacter self = bb.getSelf();

        self.stateMachine.switchState(States.BASE_MOVE_STATE);
        firstFrame = true;
    }

    @Override
    public Status execute() {
        if (firstFrame) {
            firstFrame = false;
            return Status.RUNNING;
        }
        BattleUnitBlackboard bb = getObject();
        BattleCharacter target = bb.getTarget();
        BattleCharacter self = bb.getSelf();
        if (target == null || target.isDead() || self == null || self.isDead()) {
            return Status.FAILED;
        }

        if (!self.stateMachine.getCurrent().isState(States.BASE_MOVE_STATE)) {
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
            self.stateMachine.switchState(States.NORMAL_STATE);

            return Status.SUCCEEDED;
        }
    }

    @Override
    public void end() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter target = bb.getTarget();
        BattleCharacter self = bb.getSelf();
    }

    @Override
    protected Task<BattleUnitBlackboard> copyTo(Task<BattleUnitBlackboard> task) {
        return task;
    }
}
