package io.github.some_example_name.sm.state.common;

import com.badlogic.gdx.ai.msg.Telegram;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.sm.state.BaseState;

public class NormalState implements BaseState<BattleCharacter> {
    public static final NormalState instance = new NormalState();
    private NormalState(){}
    @Override
    public void enter(BattleCharacter entity) {
        entity.currentTime = 0f;
    }

    @Override
    public boolean isState(BaseState<BattleCharacter> other) {
        return other == this;
    }

    @Override
    public void update(BattleCharacter entity, float delta) {
        entity.time+= delta;
        entity.currentTime+= delta;
    }

    @Override
    public void exit(BattleCharacter entity) {
        entity.lastStateTime = entity.currentTime;
    }

    @Override
    public boolean onMessage(BattleCharacter entity, Telegram telegram) {
        return false;
    }
}
