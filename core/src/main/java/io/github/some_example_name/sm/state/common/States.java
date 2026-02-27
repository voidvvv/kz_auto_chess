package io.github.some_example_name.sm.state.common;

import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.sm.state.BaseState;

public class States {
    public static BaseState<BattleCharacter> NORMAL_STATE = NormalState.instance;
    public static BaseState<BattleCharacter> BASE_MOVE_STATE = MoveState.INSTANCZE;

}
