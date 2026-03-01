package io.github.some_example_name.sm.state.common;

import io.github.some_example_name.battle.BattleUnitBlackboard;
import io.github.some_example_name.sm.state.BaseState;

public class States {
    public static BaseState<BattleUnitBlackboard> NORMAL_STATE = NormalState.instance;
    public static BaseState<BattleUnitBlackboard> BASE_MOVE_STATE = MoveState.INSTANCZE;

}
