package com.voidvvv.autochess.sm.state.common;

import com.badlogic.gdx.ai.msg.Telegram;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.sm.state.BaseState;

public class MoveState implements BaseState<BattleUnitBlackboard> {
    public static final MoveState INSTANCE = new MoveState();
    private MoveState(){}
    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time+= delta;
        entity.getSelf().currentTime+= delta;
    }

    @Override
    public boolean isState(BaseState<BattleUnitBlackboard> other) {
        return other == this;
    }
    @Override
    public String name() {
        return "move";
    }
    @Override
    public void enter(BattleUnitBlackboard entity) {
        entity.getSelf().moveComponent.canWalk = true;
        entity.getSelf().currentTime = 0f;
    }

    @Override
    public void exit(BattleUnitBlackboard entity) {
        entity.getSelf().moveComponent.canWalk = false;
        entity.getSelf().lastStateTime = entity.getSelf().currentTime;
    }

    @Override
    public boolean onMessage(BattleUnitBlackboard entity, Telegram telegram) {
        return false;
    }
}
