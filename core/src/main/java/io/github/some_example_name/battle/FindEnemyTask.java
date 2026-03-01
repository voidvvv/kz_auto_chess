package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.BattleUnitBlackboard;
import io.github.some_example_name.model.Battlefield;
import io.github.some_example_name.model.Card;

import java.util.List;

/**
 * 行为树叶子任务：从战场中按职业偏好选取一个敌对目标并写入黑板
 */
public class FindEnemyTask extends LeafTask<BattleUnitBlackboard> {

    @Override
    public Task.Status execute() {
        BattleUnitBlackboard bb = getObject();
        BattleCharacter self = bb.getSelf();
        Battlefield field = bb.getBattlefield();

        List<BattleCharacter> enemies = field.getOpponents(self);
        if (enemies.isEmpty()) {
            bb.setTarget(null);
            self.setTarget(null);
            return Task.Status.FAILED;
        }

        BattleCharacter chosen = chooseTarget(self, field, enemies);
        if (chosen == null) {
            bb.setTarget(null);
            self.setTarget(null);
            return Task.Status.FAILED;
        }
        bb.setTarget(chosen);
        self.setTarget(chosen);
        return Task.Status.SUCCEEDED;
    }

    /**
     * 按职业偏好选目标：战士/坦克偏前（高Y），射手偏后（选离己方底线近的敌人），刺客/法师选最近
     */
    private BattleCharacter chooseTarget(BattleCharacter self, Battlefield field, List<BattleCharacter> enemies) {
        Card.CardType type = self.getCard().getType();
        float selfY = self.getY();
        float playerTop = field.getPlayerZoneTop();

        switch (type) {
            case WARRIOR:
            case TANK:
                return chooseFrontEnemy(self, enemies, playerTop);
            case ARCHER:
                return chooseBackPreferredEnemy(self, enemies, playerTop);
            case ASSASSIN:
            case MAGE:
            default:
                return chooseClosest(self, enemies);
        }
    }

    private BattleCharacter chooseFrontEnemy(BattleCharacter self, List<BattleCharacter> enemies, float playerTop) {
        BattleCharacter best = null;
        float bestY = -Float.MAX_VALUE;
        for (BattleCharacter e : enemies) {
            if (e.isDead()) continue;
            if (e.getY() > bestY) {
                bestY = e.getY();
                best = e;
            }
        }
        return best;
    }

    private BattleCharacter chooseBackPreferredEnemy(BattleCharacter self, List<BattleCharacter> enemies, float playerTop) {
        // 射手优先打离己方更近的（Y 更小的敌方），即“前排”敌人，以便安全输出
        BattleCharacter best = null;
        float bestDist = Float.MAX_VALUE;
        for (BattleCharacter e : enemies) {
            if (e.isDead()) continue;
            float d = self.distanceTo(e);
            if (d <= self.getAttackRange() && d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        if (best != null) return best;
        return chooseClosest(self, enemies);
    }

    private BattleCharacter chooseClosest(BattleCharacter self, List<BattleCharacter> enemies) {
        BattleCharacter best = null;
        float bestD = Float.MAX_VALUE;
        for (BattleCharacter e : enemies) {
            if (e.isDead()) continue;
            float d = self.distanceTo(e);
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }

    @Override
    protected Task<BattleUnitBlackboard> copyTo(Task<BattleUnitBlackboard> task) {
        return task;
    }
}
