package com.voidvvv.autochess.battle;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;

/**
 * 为单个己方单位创建行为树：优先攻击当前目标，若无或失效则寻敌
 */
public final class UnitBehaviorTreeFactory {

    private UnitBehaviorTreeFactory() {
    }

    /**
     * 创建根为 Selector(AttackTarget, FindEnemy) 的行为树
     */
    public static BehaviorTree<BattleUnitBlackboard> create(BattleCharacter self, Battlefield battlefield) {
        BattleUnitBlackboard bb = new BattleUnitBlackboard(self, battlefield);
        return create(bb);
    }

    public static BehaviorTree<BattleUnitBlackboard> create(BattleUnitBlackboard bb) {
        Sequence<BattleUnitBlackboard> root = new Sequence<>();
        root.addChild(new FindEnemyTask());
        root.addChild(new MoveToEnemyTask());
        root.addChild(new AttackTargetTask());
//        root.cancel();`
        return new BehaviorTree<>(root, bb);
    }
}
