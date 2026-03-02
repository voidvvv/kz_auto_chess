package com.voidvvv.autochess.sm.state.common;

import com.badlogic.gdx.ai.msg.Telegram;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.sm.state.BaseState;

public class NormalState implements BaseState<BattleUnitBlackboard> {
    public static final NormalState instance = new NormalState();
    private NormalState(){}
    @Override
    public void enter(BattleUnitBlackboard entity) {
        entity.getSelf().currentTime = 0f;
    }

    @Override
    public boolean isState(BaseState<BattleUnitBlackboard> other) {
        return other == this;
    }
    @Override
    public String name() {
        return "normal";
    }
    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time+= delta;
        entity.getSelf().currentTime+= delta;
    }

    @Override
    public void exit(BattleUnitBlackboard entity) {
        entity.getSelf().lastStateTime = entity.getSelf().currentTime;
    }

    @Override
    public boolean onMessage(BattleUnitBlackboard entity, Telegram telegram) {
        return false;
    }
}
