package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.Battlefield;
import io.github.some_example_name.model.CharacterStats;
import io.github.some_example_name.model.battle.Damage;
import io.github.some_example_name.model.event.DamageEvent;
import io.github.some_example_name.msg.MessageConstants;
import io.github.some_example_name.sm.machine.BaseStateMachine;
import io.github.some_example_name.sm.machine.StateMachine;
import io.github.some_example_name.sm.state.common.AttackState;
import io.github.some_example_name.sm.state.common.States;

/**
 * 行为树黑板：持有当前单位与战场引用，供寻敌、攻击等任务使用
 */
public class BattleUnitBlackboard implements Telegraph {
    private final BattleCharacter self;
    private final Battlefield battlefield;
    private BattleCharacter target;
    public StateMachine<BattleUnitBlackboard> stateMachine;
    /**
     * 当前帧用于攻击冷却判断，由外部每帧更新
     */
    private float currentTime;

    public boolean couldDamage = false;

    public BattleUnitBlackboard(BattleCharacter self, Battlefield battlefield) {
        this.self = self;
        this.battlefield = battlefield;

        stateMachine = new BaseStateMachine<>();
        stateMachine.setOwn(this);
    }

    public BattleCharacter getSelf() {
        return self;
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public BattleCharacter getTarget() {
        return target;
    }

    public void setTarget(BattleCharacter target) {
        this.target = target;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(float t) {
        this.currentTime = t;
    }

    @Override
    public boolean handleMessage(Telegram telegram) {
        int message = telegram.message;
        switch (message) {
            case MessageConstants.attack:
                onMessageAttack(telegram);
                break;
            case MessageConstants.doAttack:
                onMessageDoAttack(telegram);
                break;
            case MessageConstants.endAttack:
                onMessageEndAttackAct(telegram);
                break;
        }
        return false;
    }

    private void onMessageEndAttackAct(Telegram telegram) {
        this.stateMachine.switchState(States.NORMAL_STATE);
    }

    private void onMessageAttack(Telegram telegram) {
        if (this.stateMachine.getCurrent().isState(AttackState.INSTANCE)) return;
        this.stateMachine.switchState(States.ATTACK_STATE);
    }
    private void onMessageDoAttack(Telegram telegram) {
        if (!couldDamage) return;
        DamageEvent de = new DamageEvent();
        de.setFrom(self);
        de.setTo(target);
        float damage = computeDamage(self, target);
        de.setDamage(new Damage((float)damage, Damage.DamageType.PhySic));
        this.getBattlefield().getDamageEventHolder().addModel(de);

    }

    private float computeDamage(BattleCharacter attacker, BattleCharacter defender) {
        CharacterStats as = attacker.getStats();

        return as.getAttack();
    }

    private void doSomething() {

    }
}
