package io.github.some_example_name.sm.state.common;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.msg.MessageConstants;
import io.github.some_example_name.sm.state.BaseState;

import java.awt.event.MouseAdapter;

public class AttackState implements BaseState<BattleCharacter> {
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

        entity.currentAttackProgress += delta;
        float maxAttackProgress = entity.maxAttackProgress;

        if (entity.currentAttackProgress>= maxAttackProgress) {
            // attack
            MessageManager.getInstance().dispatchMessage(entity.stateMachine, entity.consumer, MessageConstants.doAttack, "");
        }
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
