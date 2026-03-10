package com.voidvvv.autochess.sm.state.common;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.sm.state.AbstractState;

public class MoveState extends AbstractState {
    public static final MoveState INSTANCE = new MoveState();
    private MoveState() {}

    @Override
    public String name() {
        return "move";
    }

    @Override
    protected void onEnter(BattleUnitBlackboard entity) {
        entity.getSelf().moveComponent.canWalk = true;
    }

    @Override
    protected void onExit(BattleUnitBlackboard entity) {
        entity.getSelf().moveComponent.canWalk = false;
    }
}
