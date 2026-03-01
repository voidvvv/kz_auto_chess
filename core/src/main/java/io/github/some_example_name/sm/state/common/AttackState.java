package io.github.some_example_name.sm.state.common;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import io.github.some_example_name.model.BattleUnitBlackboard;
import io.github.some_example_name.msg.MessageConstants;
import io.github.some_example_name.sm.state.BaseState;

import java.awt.event.MouseAdapter;

public class AttackState implements BaseState<BattleUnitBlackboard> {
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

        entity.getSelf().currentAttackProgress += delta;
        float maxAttackProgress = entity.getSelf().maxAttackProgress;

        if (entity.getSelf().currentAttackProgress>= maxAttackProgress) {
            // attack
            MessageManager.getInstance().dispatchMessage(entity.getSelf().stateMachine, entity.getSelf().consumer, MessageConstants.doAttack, "");
        }
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
