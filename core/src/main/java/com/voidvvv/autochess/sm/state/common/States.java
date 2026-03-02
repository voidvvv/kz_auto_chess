package com.voidvvv.autochess.sm.state.common;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.sm.state.BaseState;

public class States {
    public static BaseState<BattleUnitBlackboard> NORMAL_STATE = NormalState.instance;
    public static BaseState<BattleUnitBlackboard> BASE_MOVE_STATE = MoveState.INSTANCE;

    // AttackState
    public static BaseState<BattleUnitBlackboard> ATTACK_STATE = AttackState.INSTANCE;

}
