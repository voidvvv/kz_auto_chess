package com.voidvvv.autochess.sm.state.common;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.sm.state.AbstractState;

public class NormalState extends AbstractState {
    public static final NormalState instance = new NormalState();
    private NormalState() {}

    @Override
    public String name() {
        return "normal";
    }
}
