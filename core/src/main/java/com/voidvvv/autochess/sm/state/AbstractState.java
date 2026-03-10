package com.voidvvv.autochess.sm.state;

import com.badlogic.gdx.ai.msg.Telegram;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;

/**
 * 状态抽象基类，提取所有具体状态的公共逻辑：
 * - 时间累加 (time / currentTime)
 * - exit 时记录 lastStateTime
 * - isState 默认引用比较（单例模式）
 * - onMessage 默认不处理
 */
public abstract class AbstractState implements BaseState<BattleUnitBlackboard> {

    @Override
    public void enter(BattleUnitBlackboard entity) {
        entity.getSelf().currentTime = 0f;
        onEnter(entity);
    }

    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time += delta;
        entity.getSelf().currentTime += delta;
        onUpdate(entity, delta);
    }

    @Override
    public void exit(BattleUnitBlackboard entity) {
        entity.getSelf().lastStateTime = entity.getSelf().currentTime;
        onExit(entity);
    }

    @Override
    public boolean isState(BaseState<BattleUnitBlackboard> other) {
        return other == this;
    }

    @Override
    public boolean onMessage(BattleUnitBlackboard entity, Telegram telegram) {
        return false;
    }

    protected void onEnter(BattleUnitBlackboard entity) {}
    protected void onUpdate(BattleUnitBlackboard entity, float delta) {}
    protected void onExit(BattleUnitBlackboard entity) {}
}
