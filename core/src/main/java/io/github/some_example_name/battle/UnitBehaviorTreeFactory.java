package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Selector;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.Battlefield;

/**
 * 为单个己方单位创建行为树：优先攻击当前目标，若无或失效则寻敌
 */
public final class UnitBehaviorTreeFactory {

    private UnitBehaviorTreeFactory() {}

    /** 创建根为 Selector(AttackTarget, FindEnemy) 的行为树 */
    public static BehaviorTree<BattleUnitBlackboard> create(BattleCharacter self, Battlefield battlefield) {
        BattleUnitBlackboard bb = new BattleUnitBlackboard(self, battlefield);
        Selector<BattleUnitBlackboard> root = new Selector<>();
        root.addChild(new AttackTargetTask());
        root.addChild(new FindEnemyTask());
        return new BehaviorTree<>(root, bb);
    }
}
