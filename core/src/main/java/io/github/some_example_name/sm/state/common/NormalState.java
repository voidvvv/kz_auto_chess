package io.github.some_example_name.sm.state.common;

import com.badlogic.gdx.ai.msg.Telegram;
import io.github.some_example_name.model.BattleUnitBlackboard;
import io.github.some_example_name.sm.state.BaseState;

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
