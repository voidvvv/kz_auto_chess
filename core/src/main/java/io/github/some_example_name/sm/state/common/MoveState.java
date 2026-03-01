package io.github.some_example_name.sm.state.common;

import com.badlogic.gdx.ai.msg.Telegram;
import io.github.some_example_name.battle.BattleUnitBlackboard;
import io.github.some_example_name.sm.state.BaseState;

public class MoveState implements BaseState<BattleUnitBlackboard> {
    public static final MoveState INSTANCZE = new MoveState();
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
