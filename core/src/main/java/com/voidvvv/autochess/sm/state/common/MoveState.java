package com.voidvvv.autochess.sm.state.common;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.logic.MovementCalculator;
import com.voidvvv.autochess.sm.state.AbstractState;

public class MoveState extends AbstractState {
    public static final MoveState INSTANCE = new MoveState();

    // 使用 MovementCalculator 检查禁锢（保持无状态）
    private static final MovementCalculator movementCalculator = new MovementCalculator();

    private MoveState() {}

    @Override
    public String name() {
        return "move";
    }

    @Override
    protected void onEnter(BattleUnitBlackboard entity) {
        // 检查是否被禁锢
//        if (movementCalculator.isImmobilized(entity.getSelf().moveComponent)) {
//            // 被禁锢时不启用移动
//            return;
//        }
        entity.getSelf().moveComponent.canWalk = true;
    }

    @Override
    protected void onExit(BattleUnitBlackboard entity) {
        entity.getSelf().moveComponent.canWalk = false;
    }
}
