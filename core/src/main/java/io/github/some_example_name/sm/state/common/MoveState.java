package io.github.some_example_name.sm.state.common;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.utils.compression.lzma.Base;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.sm.state.BaseState;

public class MoveState implements BaseState<BattleCharacter> {
    @Override
    public void update(BattleCharacter entity, float delta) {
        entity.time+= delta;
        entity.currentTime+= delta;
    }

    @Override
    public void enter(BattleCharacter entity) {
        entity.moveComponent.canWalk = true;
        entity.currentTime = 0f;
    }

    @Override
    public void exit(BattleCharacter entity) {
        entity.moveComponent.canWalk = false;
        entity.lastStateTime = entity.currentTime;
    }

    @Override
    public boolean onMessage(BattleCharacter entity, Telegram telegram) {
        return false;
    }
}
